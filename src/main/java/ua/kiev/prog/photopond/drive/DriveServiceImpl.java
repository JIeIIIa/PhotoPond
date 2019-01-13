package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryRepository;
import ua.kiev.prog.photopond.drive.exception.DirectoryModificationException;
import ua.kiev.prog.photopond.drive.exception.DriveException;
import ua.kiev.prog.photopond.drive.exception.PictureFileException;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static ua.kiev.prog.photopond.drive.DriveItemDTOMapper.toDTO;
import static ua.kiev.prog.photopond.drive.DriveServiceImpl.pathOption.CONSIDER_AS_ONE;
import static ua.kiev.prog.photopond.drive.DriveServiceImpl.pathOption.RETRIEVE_NAME;
import static ua.kiev.prog.photopond.drive.directories.Directory.*;

@Service
@Transactional
public class DriveServiceImpl implements DriveService {
    private static final Logger LOG = LogManager.getLogger(DriveServiceImpl.class);

    private final DirectoryRepository directoryRepository;

    private final PictureFileRepository fileRepository;

    private final UserInfoJpaRepository userInfoRepository;

    @Autowired
    public DriveServiceImpl(DirectoryRepository directoryRepository,
                            PictureFileRepository fileRepository,
                            UserInfoJpaRepository userInfoRepository) {
        this.directoryRepository = directoryRepository;
        this.fileRepository = fileRepository;
        this.userInfoRepository = userInfoRepository;
    }

    private Directory retrieveDirectory(String ownerLogin, String path) {
        LOG.traceEntry("Retrieve directory information for user = '{}' by path '{}'", ownerLogin, path);
        DriveUnit driveUnit = new DriveUnit(ownerLogin, path, CONSIDER_AS_ONE);
        Directory directory;

        try {
            directory = driveUnit.retrieveDirectory();
        } catch (DriveException e) {
            LOG.debug("Error in retrieving data");
            if (Directory.isRoot(path)) {
                createRootDirectoryIfNotExists(driveUnit.retrieveOwner());
                directory = driveUnit.retrieveDirectory();
            } else {
                throw e;
            }
        } catch (IllegalStateException e) {
            LOG.debug("Wrong data");
            throw new DriveException(e);
        }

        return directory;
    }

    @Override
    public List<DriveItemDTO> retrieveContent(String ownerLogin, String path, boolean withFiles) throws DriveException {
        LOG.traceEntry("Retrieve content (with files == {}) at '{}' for user = '{}'", withFiles, path, ownerLogin);
        LinkedList<DriveItemDTO> list = new LinkedList<>();

        Directory directory = retrieveDirectory(ownerLogin, path);

        directoryRepository.findTopLevelSubDirectories(directory)
                .forEach(d -> list.add(toDTO(d)));

        if (withFiles) {
            fileRepository.findByDirectory(directory)
                    .forEach(f -> list.add(toDTO(f)));
        }
        return list;
    }

    @Override
    public DirectoriesDTO retrieveDirectories(String ownerLogin, String path) throws DriveException {
        LOG.traceEntry("Retrieve directories information  at '{}' for user = '{}'");
        DirectoriesDTO directoriesDTO = new DirectoriesDTO();
        Directory directory = retrieveDirectory(ownerLogin, path);
        String baseUrl = "/api/" + ownerLogin + "/directories";


        try {
            directoriesDTO.setChildDirectories(
                    directoryRepository.findTopLevelSubDirectories(directory)
                            .stream()
                            .map(d -> toDTO(d, baseUrl))
                            .collect(toList())
            );
            directoriesDTO.setCurrent(toDTO(directory, baseUrl));

            DriveItemDTO parent = new DriveItemDTO();
            parent.setType(DriveItemType.DIR);
            parent.setName(getName(directory.parentPath()));
            parent.setParentUri(appendToPath(baseUrl, directory.parentPath()));

            directoriesDTO.setParent(parent);
        } catch (IllegalArgumentException | DataAccessException e) {
            throw new DriveException("Failure retrieve directory content", e);
        }

        return directoriesDTO;
    }

    @Override
    public DriveItemDTO addDirectory(String ownerLogin, String parentDirectoryPath, String newDirectoryName) throws DriveException {
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

        return toDTO(directoryRepository.save(newDirectory));
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
    public DriveStatisticsDTO makeStatistics(String ownerLogin) throws DriveException {
        return userInfoRepository.findByLogin(ownerLogin)
                .map(this::makeStatistics)
                .orElseThrow(DriveException::new);
    }

    private DriveStatisticsDTO makeStatistics(UserInfo userInfo) throws DriveException {
        LongSummaryStatistics statistics = directoryRepository.findByOwnerAndPathStartingWith(userInfo, SEPARATOR)
                .stream()
                .map(fileRepository::findByDirectory)
                .flatMap(Collection::stream)
                .map(fileRepository::pictureSize)
                .mapToLong(Long::longValue)
                .summaryStatistics();

        DriveStatisticsDTO driveStatisticsDTO = new DriveStatisticsDTO(userInfo.getLogin());
        driveStatisticsDTO.setDirectoriesSize(statistics.getSum());
        driveStatisticsDTO.setPictureCount(statistics.getCount());

        return driveStatisticsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriveStatisticsDTO> fullStatistics() throws DriveException {
        return userInfoRepository.findAll().stream()
                .map(this::makeStatistics)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] retrievePictureFileData(String ownerLogin, String path) throws DriveException {
        LOG.traceEntry("Picture file = '{}'  |  owner = {}", path, ownerLogin);

        return findPictureFile(ownerLogin, path).getData();
    }

    @Override
    public DriveItemDTO addPictureFile(String ownerLogin, String directoryPath, MultipartFile multipartFile) throws DriveException {
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

        return toDTO(fileRepository.save(newFile));
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
                this.owner = userInfoRepository.findByLogin(this.ownerLogin)
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
