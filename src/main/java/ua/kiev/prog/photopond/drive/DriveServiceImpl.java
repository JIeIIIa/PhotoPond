package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryDiskAndDatabaseRepository;
import ua.kiev.prog.photopond.drive.directories.DirectoryModificationException;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileException;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static ua.kiev.prog.photopond.drive.DriveServiceImpl.pathOption.CONSIDER_AS_ONE;
import static ua.kiev.prog.photopond.drive.DriveServiceImpl.pathOption.RETRIEVE_NAME;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@Service
@Transactional
public class DriveServiceImpl implements DriveService {
    private static final Logger LOG = LogManager.getLogger(DriveServiceImpl.class);

    private final DirectoryDiskAndDatabaseRepository directoryRepository;

    private final PictureFileRepository fileRepository;

    private final UserInfoService userInfoService;

    @Autowired
    public DriveServiceImpl(DirectoryDiskAndDatabaseRepository directoryRepository,
                            PictureFileRepository fileRepository,
                            UserInfoService userInfoService) {
        this.directoryRepository = directoryRepository;
        this.fileRepository = fileRepository;
        this.userInfoService = userInfoService;
    }

    @Override
    public Content retrieveDirectoryContent(String ownerLogin, String path) throws DriveException {
        LOG.traceEntry("Content at '{}' for user = '{}'", path, ownerLogin);

        DriveUnit driveUnit = new DriveUnit(ownerLogin, path, CONSIDER_AS_ONE);
        Directory directory = null;

        try {
            directory = driveUnit.retrieveDirectory();
        } catch (DriveException e) {
            if (Directory.isRoot(path)) {
                createRootDirectoryIfNotExists(driveUnit.retrieveOwner());
                directory = driveUnit.retrieveDirectory();
            }
        }

        return getDirectoryContent(directory);
    }

    @Override
    public Directory addDirectory(String ownerLogin, String parentDirectoryPath, String newDirectoryName) throws DriveException {
        LOG.traceEntry("Parameters: ownerLogin = {};   parentDirectoryPath = {}, newDirectoryName = {}",
                ownerLogin, parentDirectoryPath, newDirectoryName);
        DriveUnit source = new DriveUnit(ownerLogin, parentDirectoryPath, CONSIDER_AS_ONE);

        String path = buildPath(parentDirectoryPath, newDirectoryName);

        if (directoryRepository.exists(source.retrieveOwner(), path)) {
            String message = "Directory '" + path + "' already exists (owner = " + source.retrieveOwner().getLogin() + ")";
            LOG.warn(message);
            throw new DriveException(message);
        }
        Directory newDirectory = new DirectoryBuilder()
                .owner(source.retrieveOwner())
                .path(path)
                .build();

        return directoryRepository.save(newDirectory);
    }

    @Override
    public void moveDirectory(String ownerLogin, String source, String target) throws DriveException {
        LOG.traceEntry("ownerLogin = {}  |  source = '{}'  |  target = {}", ownerLogin, source, target);
        DriveUnit sourceUnit = new DriveUnit(ownerLogin, source, CONSIDER_AS_ONE);

        DriveUnit targetUnit = new DriveUnit(ownerLogin, target, RETRIEVE_NAME);
        Directory targetDirectory = targetUnit.retrieveDirectory();

        try {
            if (sourceUnit.retrieveDirectory().parentPath().equals(targetDirectory.getPath())) {
                LOG.debug("Try to rename");
                directoryRepository.rename(sourceUnit.retrieveDirectory(), target);
            } else {
                LOG.debug("Try to move");
                directoryRepository.move(sourceUnit.retrieveDirectory(), targetDirectory);
            }
        } catch (DriveException e) {
            String message = "Failure moving directory: " + source + " -> " + target;
            LOG.debug(message);
            throw new DriveException(message, e);
        }
    }


    @Override
    public void deleteDirectory(String ownerLogin, String source) throws DriveException {
        LOG.traceEntry("Try to delete the directory:   owner = {}   source = {}", ownerLogin, source);
        DriveUnit sourceUnit = new DriveUnit(ownerLogin, source, CONSIDER_AS_ONE);
        try {
            directoryRepository.delete(sourceUnit.retrieveDirectory());
        } catch (DriveException e) {
            String message = "Failure deleting directory " + source + " for owner = " + ownerLogin;
            LOG.warn(message);
            throw new DriveException(message, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] retrievePictureFileData(String ownerLogin, String path) throws DriveException {
        LOG.traceEntry("Picture file = '{}'  |  owner = {}", path, ownerLogin);

        return findPictureFile(ownerLogin, path).getData();
    }

    @Override
    public PictureFile addPictureFile(String ownerLogin, String directoryPath, MultipartFile multipartFile) throws DriveException {
        LOG.traceEntry("Try to save file into the directory '{}' for owner = {}", directoryPath, ownerLogin);
        DriveUnit sourceParts = new DriveUnit(ownerLogin, directoryPath, CONSIDER_AS_ONE);
        PictureFile newFile;

        try {
            newFile = PictureFileBuilder.getInstance()
                    .directory(sourceParts.retrieveDirectory())
                    .filename(multipartFile.getOriginalFilename())
                    .data(multipartFile.getBytes())
                    .build();
        } catch (IOException e) {
            String message = "Failure retrieve data for saving into the directory '" + directoryPath + "' for owner = " + ownerLogin;
            LOG.error(message);
            throw new DriveException(message, new PictureFileException(e));
        }

        return fileRepository.save(newFile);
    }

    @Override
    public void movePictureFile(String ownerLogin, String path, String newPath) throws DriveException {
        LOG.traceEntry("Owner = {}   Try to move: {}  ->  {}", ownerLogin, path, newPath);
        PictureFile file = findPictureFile(ownerLogin, path);
        DriveUnit target = new DriveUnit(ownerLogin, newPath, RETRIEVE_NAME);

        try {
            fileRepository.move(file, target.retrieveDirectory(), target.retrieveFilename());
        } catch (PictureFileException e) {
            String message = "Failure moving file: " + path + " -> " + newPath + "   for owner = " + ownerLogin;
            LOG.warn(message + "   " + e.getMessage());
            throw new DriveException(message, e);
        }
    }

    @Override
    public void deletePictureFile(String ownerLogin, String path) throws DriveException {
        LOG.traceEntry("Try to delete the picture file:   owner = {}   path = {}", ownerLogin, path);
        PictureFile file = findPictureFile(ownerLogin, path);

        try {
            fileRepository.delete(file);
        } catch (PictureFileException e) {
            String message = "Failure deleting picture file " + path + " for owner = " + ownerLogin;
            LOG.warn(message);
            throw new DriveException(message, e);
        }
    }

    private Content getDirectoryContent(Directory current) throws DriveException {
        LOG.traceEntry("Get content for {}", current);
        Content content = new Content();

        content.setCurrentDirectory(current);
        content.setParents(findParentDirectories(current));
        content.setTopSubDirectories(directoryRepository.findTopLevelSubDirectories(current));
        content.setFiles(fileRepository.findByDirectory(current));

        return LOG.traceExit(content);
    }

    private void createRootDirectoryIfNotExists(UserInfo owner) throws DriveException {
        LOG.traceEntry("Check and try create root directory: owner={} ", owner);
        List<Directory> directories = directoryRepository.findByOwnerAndPath(owner, SEPARATOR);
        if (directories.size() == 0) {
            Directory root = new DirectoryBuilder()
                    .owner(owner)
                    .path(SEPARATOR)
                    .build();
            try {
                LOG.debug("Try to create root directory for {}", owner);
                directoryRepository.save(root);
            } catch (DirectoryModificationException e) {
                String message = "User " + owner + " has no content and failed to create root directory";
                LOG.error(message);
                throw new DriveException(message, e);
            }
        } else if (directories.size() == 1) {
            LOG.debug("Root directory already exists (owner = {})", owner.getLogin());
        } else {
            String message = "Found two or more root directories for user " + owner;
            LOG.error(message);
            throw new DriveException(message);
        }
    }

    private List<Directory> findParentDirectories(Directory directory) throws DriveException {
        LOG.traceEntry("Find parent directories for '{}'", directory);
        List<Directory> list = new LinkedList<>();
        if (directory == null || directory.isRoot()) {
            LOG.debug("return empty list");
            return list;
        }
        Directory current = directory;
        do {
            List<Directory> parents = directoryRepository.findByOwnerAndPath(current.getOwner(), current.parentPath());
            if (parents.size() != 1) {
                LOG.debug("Failed to get parent directory for '{}'", current.parentPath());
                throw new DriveException("Not found or found more than one parent directory for " + current);
            }
            current = parents.get(0);
            list.add(0, current);
            LOG.trace("Add parent directory '{}' in list for {}", current, directory);
        } while (!current.isRoot());
        return LOG.traceExit(list);
    }

    private PictureFile findPictureFile(String ownerLogin, String path) {
        DriveUnit source = new DriveUnit(ownerLogin, path, RETRIEVE_NAME);
        List<PictureFile> files = fileRepository.findByDirectoryAndFilename(source.retrieveDirectory(), source.retrieveFilename());
        if (files.size() == 0) {
            String message = "File not found for ownerLogin = " + ownerLogin + "   and   filename = " + path;
            LOG.debug(message);
            throw new DriveException(message);
        } else if (files.size() > 1) {
            String message = "Found more than one file for ownerLogin = " + ownerLogin
                    + "   and   path filename = " + path;
            LOG.error(message);
            throw new DriveException(message);
        }

        return files.get(0);
    }
    enum pathOption {
        CONSIDER_AS_ONE, RETRIEVE_NAME
    }

    private class DriveUnit {
        pathOption pathOption;

        private String ownerLogin;

        private String path;

        private UserInfo owner;

        private Directory directory;

        private String filename;

        DriveUnit(String ownerLogin, String path, pathOption pathOption) {
            this.ownerLogin = ownerLogin;
            this.path = path;
            this.pathOption = pathOption;
        }

        UserInfo retrieveOwner() {
            if (this.owner == null) {
                this.owner = userInfoService.findUserByLogin(this.ownerLogin)
                        .orElseThrow(() -> new DriveException(new IllegalArgumentException("User with login '" + this.ownerLogin + "' not found")));
            }
            return this.owner;
        }

        Directory retrieveDirectory() throws DriveException {
            if (directory == null) {
                extractDirectoryAndFilename();
            }
            return directory;
        }

        String retrieveFilename() throws DriveException {
            if (filename == null) {
                extractDirectoryAndFilename();
            }
            return filename;
        }

        private void extractDirectoryAndFilename() throws DriveException {
            verifyPath();

            List<Directory> directories;
            String directoryPath;
            if (pathOption == CONSIDER_AS_ONE) {
                this.filename = "";
                directoryPath = path;
            } else {
                int lastSeparatorIndex = path.lastIndexOf(SEPARATOR);
                this.filename = path.substring(lastSeparatorIndex + 1);

                if (lastSeparatorIndex == 0) {
                    directoryPath = SEPARATOR;
                } else {
                    directoryPath = path.substring(0, lastSeparatorIndex);
                }
            }
            directories = directoryRepository.findByOwnerAndPath(retrieveOwner(), directoryPath);
            if (directories.size() != 1) {
                throw new DriveException("Not found or found more than one directory for owner = " + this.owner + "   and   directoryPath = " + directoryPath);
            }
            this.directory = directories.get(0);
        }

        private void verifyPath() {
            if (path == null || path.isEmpty()) {
                throw new DriveException("Path is null or empty");
            }
            if (!path.contains(SEPARATOR)) {
                throw new DriveException("Wrong file path");
            }

        }

        @Override
        public String toString() {
            return "DriveUnit{" +
                    "owner=" + owner +
                    "directory=" + directory +
                    ", filename='" + filename + '\'' +
                    '}';
        }
    }
}
