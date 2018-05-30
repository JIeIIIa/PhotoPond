package ua.kiev.prog.photopond.drive.directories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.user.UserInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static ua.kiev.prog.photopond.Utils.Utils.deleteDirectoryWithContents;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.retrieveParentPath;

@Repository
public class DirectoryDiskAndDatabaseRepositoryImpl implements DirectoryDiskAndDatabaseRepository {
    private static final Logger log = LogManager.getLogger(DirectoryDiskAndDatabaseRepositoryImpl.class);

    @Value("${folders.basedir.location}")
    private String foldersBaseDir;

    private final DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    public DirectoryDiskAndDatabaseRepositoryImpl(DirectoryJpaRepository directoryJpaRepository) {
        log.traceEntry("Constructor with parameter: {}", directoryJpaRepository);
        this.directoryJpaRepository = directoryJpaRepository;
    }

    public void setFoldersBasedir(String foldersBaseDir) {
        this.foldersBaseDir = foldersBaseDir;
    }

    @Override
    public Directory save(Directory directory) throws DirectoryModificationException {
        log.traceEntry("Save {}", directory);
        throwExceptionIfDirectoryNull(directory);

        if (directory.getLevel() > 1 &&
                directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.parentPath()).size() != 1) {
            log.debug("Not exists parent directory for current saved directory {}", directory);
            throw new DirectoryModificationException("Not exists parent directory for current saved directory");
        }

        Path pathOnDisk = Paths.get(foldersBaseDir + directory.getFullPath());
        if (Files.notExists(pathOnDisk, LinkOption.NOFOLLOW_LINKS)) {
            log.trace("Try to create directory:   {}", pathOnDisk);
            try {
                Files.createDirectories(pathOnDisk);
            } catch (IOException e) {
                log.debug("Cannot create directory on disk:   {}", pathOnDisk);
                throw new DirectoryModificationException("Cannot create directory on disk", e);
            }
            log.trace("Directory was created on disk:   {}", pathOnDisk);
        }
        directory = directoryJpaRepository.save(directory);
        return log.traceExit("Directory was saved in database:   {}", directory);
    }

    private void throwExceptionIfDirectoryNull(Directory directory) {
        if (directory == null) {
            log.debug("Directory:   NULL");
            throw new IllegalArgumentException("directory is null");
        }
    }

    @Override
    public void delete(Directory directory) throws DirectoryModificationException {
        log.traceEntry("Delete {}", directory);
        throwExceptionIfDirectoryNull(directory);


        Path pathOnDisk;
        try {
            pathOnDisk = Paths.get(foldersBaseDir + directory.getFullPath());
            List<Directory> directoriesToDelete = directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath());
            //directoryJpaRepository.deleteInBatch(directoriesToDelete);
            directoryJpaRepository.deleteAll(directoriesToDelete);
            log.trace("Directory with contents was deleted from database:   {}", directory);
        } catch (Exception e) {
            log.debug("Failure deleting from jpa repository for '{}'", directory);
            throw new DirectoryModificationException("Failure deleting from jpa repository", e);
        }
        try {
            deleteDirectoryWithContents(pathOnDisk);
            log.trace("Directory with contents was deleted from disk:   {}", directory);
        } catch (IOException e) {
            log.debug("Failure deleting from disk for '{}'", directory);
            throw new DirectoryModificationException("Failure deleting from disk repository", e);
        }
        log.traceExit();
    }

    @Override
    public void rename(Directory directory, String newPath) throws DirectoryModificationException {
        log.traceEntry("Rename:   {}   ->   {}", directory, newPath);
        throwExceptionIfDirectoryNull(directory);

        String newName = Directory.getName(newPath);
        if (directory.getName().equals(newName)) {
            log.traceExit("New and old names are same:  {}", directory);
            return;
        }

        OperationParameters parameters = new OperationParameters(directory, retrieveParentPath(newPath), newName);

        isPossibleToRename(parameters);
        moveDirectories(directory, parameters, "Rename: cannot rename directory on disk");
        log.traceExit();
    }

    private void isPossibleToRename(OperationParameters parameters) throws DirectoryModificationException {
        if (!Files.exists(parameters.getCurrentPathOnDisk())) {
            log.debug("Rename:   directory with name '{}' did not find on disk", parameters.getCurrentPathOnDisk());
            throw new DirectoryModificationException("Rename - directory with name '" + parameters.getCurrentPathOnDisk() + "' did not find on disk");
        }
        if (Files.exists(parameters.getTargetPathOnDisk())) {
            log.debug("Rename:   directory on path '{}' already exists on disk", parameters.getCurrentPathOnDisk());
            throw new DirectoryModificationException("Rename - directory on path '" + parameters.getTargetPathOnDisk() + "' already exists on disk");
        }
        if (directoryJpaRepository.findByOwnerAndPath(parameters.getOwner(), parameters.getTargetPath()).size() > 0) {
            log.debug("Rename:   directory on path '{}' already exists in database", parameters.getCurrentPathOnDisk());
            throw new DirectoryModificationException("Rename - directory on path '" + parameters.getTargetPath() + "' already exists in database");
        }
        String parentDirectoryPath = retrieveParentPath(parameters.getTargetPath());
        if (directoryJpaRepository.findByOwnerAndPath(parameters.getOwner(), parentDirectoryPath).isEmpty()) {
            log.debug("Rename:   not found parent directory '{}'", parentDirectoryPath);
            throw new DirectoryModificationException("Rename - not found parent directory '" + parentDirectoryPath + "'");
        }
        log.traceExit();
    }

    @Override
    public void move(Directory source, Directory target) throws DirectoryModificationException {
        log.traceEntry("Move directory:   " + source);
        throwExceptionIfDirectoryNull(source);
        throwExceptionIfDirectoryNull(target);

        if (source.equals(target)) {
            log.traceExit("Source and target are equal:  {}", source);
            return;
        }

        isPossibleToMove(source, target);
        OperationParameters parameters = new OperationParameters(source, target.getPath(), source.getName());

        moveDirectories(source, parameters, "Move: cannot move directory or subdirectories on disk");
        log.traceExit();
    }

    private void isPossibleToMove(Directory source, Directory target) throws DirectoryModificationException {
        log.traceEntry();
        if (!source.getOwner().equals(target.getOwner())) {
            log.debug("Source and target directories must have same owner!   source={},   Target={}", source, target);
            throw new DirectoryModificationException("Cannot move directory", new IllegalAccessException("Source and target directories must have same owner"));
        }
        if (!directoryJpaRepository.findByOwnerAndPath(source.getOwner(), target.getPath() + SEPARATOR + source.getName()).isEmpty()) {
            log.debug("Target [{}] contains directory with same name='{}'", target, source.getName());
            throw new DirectoryModificationException("Target directory contains directory with same name");
        }
        log.traceExit();
    }

    private void moveDirectories(Directory source, OperationParameters parameters, String error) throws DirectoryModificationException {
        log.traceEntry("{}     {}", source, parameters);
        try {
            source.setPath(parameters.getTargetPath());
            replaceRelatedDirectoryPaths(parameters);
            log.trace("Renamed source path on entity. New path: {}", source.getPath());
            Files.move(parameters.getCurrentPathOnDisk(), parameters.getTargetPathOnDisk());
            log.trace("Moved directory on disk '{}' -> '{}'", parameters.getCurrentPathOnDisk(), parameters.getTargetPathOnDisk());
        } catch (IOException e) {
            log.debug("Exception while moving directory", e);
            throw new DirectoryModificationException(error, e);
        }
        log.traceExit();
    }

    private void replaceRelatedDirectoryPaths(OperationParameters parameters) {
        log.traceEntry("Replace related directory paths   {}", parameters.toString());
        List<Directory> directories = directoryJpaRepository
                .findByOwnerAndPathStartingWith(parameters.getOwner(), parameters.getCurrentPath() + SEPARATOR);
        for (Directory dir : directories) {
            String pathAfterRename = dir.getPath().replaceFirst(parameters.currentPath, parameters.getTargetPath());
            log.trace("Change path: '{}' -> '{}'", dir.getPath(), pathAfterRename);
            dir.setPath(pathAfterRename);
        }
        log.traceExit();
    }

    @Override
    public List<Directory> findTopSubDirectories(Directory directory) {
        log.traceEntry("Directory:   " + directory);
        String path;
        if (directory.isRoot()) {
            path = directory.getPath();
        } else {
            path = directory.getPath() + SEPARATOR;
        }
        List<Directory> topSubDirectories = directoryJpaRepository.findByOwnerAndPathStartingWithAndLevel(
                directory.getOwner(),
                path,
                directory.getLevel() + 1
        );
        log.debug("Count top-level subdirectories = {}      for {}", topSubDirectories.size(), directory);
        return log.traceExit(topSubDirectories);
    }

    @Override
    public long countByOwner(UserInfo owner) {
        log.traceEntry("Count by {}", owner);
        long count = directoryJpaRepository.countByOwner(owner);

        return log.traceExit(count);
    }

    @Override
    public List<Directory> findByOwnerAndPath(UserInfo owner, String path) {
        log.traceEntry("Find by owner = {} and path = '{}'", owner, path);
        return directoryJpaRepository.findByOwnerAndPath(owner, path);
    }

    @Override
    public Directory findById(Long directoryId) throws DirectoryException {
        Optional<Directory> directory = directoryJpaRepository.findById(directoryId);
        if (!directory.isPresent()) {
            throw new DirectoryException("Not found directory with id = " + directoryId);
        }
        return directory.get();
    }

    @Override
    public Directory findByOwnerAndId(UserInfo owner, Long directoryId) throws DirectoryException {
        Optional<Directory> directory = directoryJpaRepository.findById(directoryId);
        if (!directory.isPresent()) {
            throw new DirectoryException("Not found directory with id = " + directoryId + " for owner = " + owner);
        }
        if (!Files.exists(Paths.get(foldersBaseDir + directory.get().getFullPath()))) {
            throw new DirectoryException("Not found " + directory.get() + " on disk");
        }
        return directory.get();
    }

    private class OperationParameters {

        private final UserInfo owner;
        private final String ownerFolder;
        private final String currentPath;
        private final String targetPath;

        OperationParameters(Directory directory, String newParentPath, String newName) {
            ownerFolder = directory.getOwnerFolder();
            currentPath = directory.getPath();
            if (SEPARATOR.equals(newParentPath)) {
                targetPath = SEPARATOR + newName;
            } else {
                targetPath = newParentPath + SEPARATOR + newName;
            }

            owner = directory.getOwner();
        }

        public UserInfo getOwner() {
            return owner;
        }

        String getTargetPath() {
            return targetPath;
        }

        String getFullTargetPath() {
            return foldersBaseDir + ownerFolder + targetPath;
        }

        Path getTargetPathOnDisk() {
            return Paths.get(getFullTargetPath());
        }

        String getCurrentPath() {
            return currentPath;
        }

        public String getFullCurrentPath() {
            return foldersBaseDir + ownerFolder + currentPath;
        }

        Path getCurrentPathOnDisk() {
            return Paths.get(getFullCurrentPath());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OperationParameters that = (OperationParameters) o;

            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            if (ownerFolder != null ? !ownerFolder.equals(that.ownerFolder) : that.ownerFolder != null) return false;
            if (currentPath != null ? !currentPath.equals(that.currentPath) : that.currentPath != null) return false;
            return targetPath != null ? targetPath.equals(that.targetPath) : that.targetPath == null;
        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (ownerFolder != null ? ownerFolder.hashCode() : 0);
            result = 31 * result + (currentPath != null ? currentPath.hashCode() : 0);
            result = 31 * result + (targetPath != null ? targetPath.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "OperationParameters{" +
                    "owner=" + owner.getLogin() +
                    ", ownerFolder='" + ownerFolder + '\'' +
                    ", currentPath='" + currentPath + '\'' +
                    ", targetPath='" + targetPath + '\'' +
                    '}';
        }
    }
}
