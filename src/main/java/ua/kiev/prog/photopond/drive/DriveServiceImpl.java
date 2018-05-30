package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.directories.*;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileException;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@Service
@Transactional
public class DriveServiceImpl implements DriveService {
    private static final Logger log = LogManager.getLogger(DriveServiceImpl.class);

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
    @Transactional(readOnly = true)
    public long countByOwner(UserInfo owner) {
        log.traceEntry("Count directories by {}", owner);
        long count = directoryRepository.countByOwner(owner);
        return log.traceExit(count);
    }

    @Override
    public Content getDirectoryContent(String ownerLogin, String path) throws DriveException {
        log.traceEntry("Content at '{}' for user = '{}'", path, ownerLogin);

        FileParts fileParts = null;
        Directory directory;
        try {
            fileParts = new FileParts(ownerLogin, path + SEPARATOR);
            directory = fileParts.getDirectory();
        } catch (DriveException e) {
            directory = createRootDirectoryIfNotExists(fileParts.getOwner());
        }
        Content content = getDirectoryContent(directory);
        return content;
    }

    @Override
    public Directory addDirectory(String ownerLogin, String parentDirectoryPath, String newDirectoryName) throws DriveException {
        FileParts sourceParts = new FileParts(ownerLogin, parentDirectoryPath + SEPARATOR);

        Directory directory = sourceParts.getDirectory();
        String path = buildPath(directory.getPath(), newDirectoryName);

        List<Directory> currentDirectory = directoryRepository.findByOwnerAndPath(sourceParts.getOwner(), path);
        Directory newDirectory;
        if(currentDirectory.isEmpty()) {
             newDirectory = new DirectoryBuilder()
                    .owner(sourceParts.getOwner())
                    .path(path)
                    .build();
            newDirectory = directoryRepository.save(newDirectory);
        } else {
            throw new DirectoryException("Directory '" + path + "' already exists");
        }
        return newDirectory;
    }

    @Override
    public void moveDirectory(String ownerLogin, String source, String target) throws DriveException {
        FileParts sourceParts = new FileParts(ownerLogin, source + SEPARATOR);
        Directory targetDirectory = null;
        try {
            FileParts targetParts = new FileParts(ownerLogin, target /*+ SEPARATOR*/);
            targetDirectory = targetParts.getDirectory();
        } catch (DriveException e) {
            /*targetDirectory = new DirectoryBuilder()
                    .owner(sourceParts.getOwner())
                    .path(target)
                    .build();*/
        }

        try {
            if (targetDirectory == null) {
                directoryRepository.rename(sourceParts.getDirectory(), target);
            } else {
                directoryRepository.move(sourceParts.getDirectory(), targetDirectory);
            }
        } catch (DriveException e) {
            log.debug("Failure moving directory: {} -> {}", source, target);
            throw new DriveException("Failure moving directory: " + source + " -> " + target);
        }
    }


    @Override
    public PictureFile addPictureFile(String ownerLogin, String directoryPath, MultipartFile multipartFile) throws DriveException {
        FileParts sourceParts = new FileParts(ownerLogin, directoryPath + SEPARATOR);
        PictureFile newFile;

        try {
             newFile = PictureFileBuilder.getInstance()
                    .directory(sourceParts.getDirectory())
                    .filename(multipartFile.getOriginalFilename())
                    .data(multipartFile.getBytes())
                    .build();

        } catch (IOException e) {
            throw new PictureFileException(e);
        }

        newFile = fileRepository.save(newFile);

        return newFile;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFile(String ownerLogin, String path) throws DriveException {
        FileParts source = new FileParts(ownerLogin, path);
        List<PictureFile> files = fileRepository.findByDirectoryAndFilename(source.getDirectory(), source.getFilename());
        if (files.size() != 1) {
            throw new DriveException("Found more than one file for ownerLogin = " + ownerLogin + "   and   filename = " + source.getFilename());
        }

        return files.get(0).getData();
    }

    @Override
    @Modifying
    public void deletePictureFile(String ownerLogin, String path) throws DriveException {
        FileParts source = new FileParts(ownerLogin, path);
        try {
            fileRepository.deleteByDirectoryAndFilename(source.getDirectory(), source.getFilename());
        } catch (PictureFileException e) {
            throw new PictureFileException(e);
        }
    }

    @Override
    @Modifying
    public void movePictureFile(String ownerLogin, String path, String newPath) throws DriveException {
        FileParts source = new FileParts(ownerLogin, path);
        FileParts target = new FileParts(ownerLogin, newPath);
        try {
            fileRepository.move(source.getDirectory(), source.getFilename(), target.getDirectory(), target.getFilename());
        } catch (PictureFileException e) {
            throw new DriveException("a", e);
        }
    }

    @Override
    public void deleteDirectory(String ownerLogin, String path) throws DriveException {
        FileParts source = new FileParts(ownerLogin, buildPath(path, "mock.name"));
        try {
            directoryRepository.delete(source.getDirectory());
        } catch (DriveException e) {
            throw new DriveException("b", e);
        }
    }

    private Content getDirectoryContent(Directory current) throws DriveException {
        log.traceEntry("Get content for {}", current);
        Content content = new Content();
        content.setCurrentDirectory(current);

        List<Directory> parentDirectories = findParentDirectories(current);
        content.setParents(parentDirectories);

        List<Directory> topSubDirectories = directoryRepository.findTopSubDirectories(current);
        content.setTopSubDirectories(topSubDirectories);

        List<PictureFile> files = fileRepository.findByDirectory(current);
        content.setFiles(files);

        return log.traceExit(content);
    }

    @Modifying
    private Directory createRootDirectoryIfNotExists(UserInfo owner) throws DriveException {
        log.traceEntry("Check and try create root directory: owner={} ", owner);
        long count = directoryRepository.countByOwner(owner);
        Directory root = null;
        if (count == 0) {
            root = new DirectoryBuilder()
                    .owner(owner)
                    .path(SEPARATOR)
                    .build();
            try {
                log.debug("Try to create root directory for {}", owner);
                root = directoryRepository.save(root);
            } catch (DirectoryModificationException e) {
                log.debug("User {} has no content and failed to create root directory", owner);
                throw new DriveException("Failed to create root directory", e);
            }
        }
        return root;
    }

    private List<Directory> findParentDirectories(Directory directory) throws DriveException {
        log.traceEntry("Find parent directories for '{}'", directory);
        List<Directory> list = new LinkedList<>();
        if (directory.isRoot()) {
            return list;
        }
        Directory current = directory;
        do {
            List<Directory> parents = directoryRepository.findByOwnerAndPath(current.getOwner(), current.parentPath());
            if (parents.size() != 1) {
                log.debug("Failed to get parent directory for '{}'", current.parentPath());
                throw new DriveException("Not found or found more than one parent directory for " + current);
            }
            current = parents.get(0);
            list.add(0, current);
            log.trace("Add parent directory '{}' in list", current);
        } while (!current.isRoot());
        return list;
    }

    class FileParts {
        private String ownerLogin;

        private String path;

        private UserInfo owner;

        private Directory directory;

        private String filename;

        private FileParts(String ownerLogin, String path) {
            this.ownerLogin = ownerLogin;
            this.path = path;
            this.owner = null;
            this.directory = null;
            this.filename = null;
        }

        public UserInfo getOwner() {
            if (this.owner == null) {
                this.owner = userInfoService.getUserByLogin(ownerLogin).get();
            }
            return this.owner;
        }

        public Directory getDirectory() throws DriveException {
            if (directory == null) {
                extractDirectoryAndFilename();
            }
            return directory;
        }

        public String getFilename() throws DriveException {
            if (filename == null) {
                extractDirectoryAndFilename();
            }
            return filename;
        }

        private void extractDirectoryAndFilename() throws DriveException {
            if (path == null || path.isEmpty()) {
                throw new PictureFileException("Path is null or empty");
            }
            int lastSeparatorIndex = path.lastIndexOf(SEPARATOR);
            if (lastSeparatorIndex < 0) {
                throw new PictureFileException("Wrong file path");
            }

            String directoryPath = path.substring(0, lastSeparatorIndex);
            if (directoryPath.isEmpty()) {
                directoryPath = SEPARATOR;
            }
            List<Directory> directories = directoryRepository.findByOwnerAndPath(getOwner(), directoryPath);
            if (directories.size() != 1) {
                throw new DriveException("Not found or found more than one directory for owner = " + this.owner + "   and   directoryPath = " + directoryPath);
            }
            this.directory = directories.get(0);
            this.filename = path.substring(lastSeparatorIndex+1);
        }

        @Override
        public String toString() {
            Directory directory = null;
            String filename = null;
            try {
                directory = getDirectory();
                filename = getFilename();
            } catch (DriveException ignored) {
                /*NOP*/
            }
            return "FileParts{" +
                    "directory=" + directory +
                    ", filename='" + filename + '\'' +
                    '}';
        }
    }
}
