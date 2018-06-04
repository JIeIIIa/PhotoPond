package ua.kiev.prog.photopond.drive.directories;

import org.apache.commons.io.FileUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ua.kiev.prog.photopond.drive.directories.Directory.*;

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

    void setFoldersBasedir(String foldersBaseDir) {
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
            List<Directory> directoriesToDelete = directoryJpaRepository.findByOwnerAndPathStartingWith(
                    directory.getOwner(), directory.getPath() + SEPARATOR
            );
            directoriesToDelete.add(directory);
            directoryJpaRepository.deleteAll(directoriesToDelete);
            log.trace("Directory with contents was deleted from database:   {}", directory);
        } catch (Exception e) {
            log.debug("Failure deleting from jpa repository for '{}'", directory);
            throw new DirectoryModificationException("Failure deleting from jpa repository", e);
        }
        try {
            FileUtils.deleteDirectory(pathOnDisk.toFile());
            log.trace("Directory with contents was deleted from disk:   {}", directory);
        } catch (IOException | IllegalArgumentException e) {
            log.error("Failure deleting from disk for '{}'", directory);
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

        OperationArgumentsVO parameters = new OperationArgumentsVO(directory, retrieveParentPath(newPath), newName);

        isPossibleToRename(parameters);
        moveDirectories(parameters, "Rename: cannot rename directory on disk");
        log.traceExit();
    }

    private void isPossibleToRename(OperationArgumentsVO parameters) throws DirectoryModificationException {
        if (!Files.exists(parameters.currentPathOnDisk())) {
            log.debug("Rename:   directory with name '{}' not found on disk", parameters.currentPathOnDisk());
            throw new DirectoryModificationException("Cannot rename directory",
                    new IllegalAccessException("Directory with name '" + parameters.currentPathOnDisk() + "' not found on disk")
            );
        }
        if (directoryJpaRepository.findByOwnerAndPath(parameters.owner(), parameters.targetPath()).size() > 0) {
            log.debug("Rename:   directory on path '{}' already exists in database for user {}", parameters.targetPath(), parameters.owner.getLogin());
            throw new DirectoryModificationException("Cannot rename directory",
                    new IllegalAccessException("Directory on path '" + parameters.targetPath() + "' already exists in database for user '" + parameters.owner.getLogin() + "'")
            );
        }
        String parentDirectoryPath = retrieveParentPath(parameters.targetPath());
        if (directoryJpaRepository.findByOwnerAndPath(parameters.owner(), parentDirectoryPath).isEmpty()) {
            log.debug("Rename:   not found parent directory '{}'", parentDirectoryPath);
            throw new DirectoryModificationException("Cannot rename directory",
                    new IllegalAccessException("Not found parent directory '" + parentDirectoryPath + "'")
            );
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
        OperationArgumentsVO parameters = new OperationArgumentsVO(source, target.getPath(), source.getName());

        moveDirectories(parameters, "Move: cannot move directory or subdirectories on disk");
        log.traceExit();
    }

    private void isPossibleToMove(Directory source, Directory target) throws DirectoryModificationException {
        log.traceEntry();
        if (!source.getOwner().equals(target.getOwner())) {
            log.debug("Source and target directories must have same owner!   source={},   Target={}", source, target);
            throw new DirectoryModificationException("Cannot move directory",
                    new IllegalAccessException("Source and target directories must have same owner")
            );
        }
        if (target.getPath().startsWith(source.getPath())) {
            log.debug("Target [{}] is subdirectory fot [{}]", target.getPath(), source.getPath());
            throw new DirectoryModificationException("Cannot move directory",
                    new IllegalAccessException("Target [" + target.getPath() + "]directory is subdirectory for [" + source.getPath() + "]")
            );
        }
        if (!directoryJpaRepository.findByOwnerAndPath(source.getOwner(), target.getPath() + SEPARATOR + source.getName()).isEmpty()) {
            log.debug("Target [{}] contains directory with same name='{}'", target, source.getName());
            throw new DirectoryModificationException("Cannot move directory",
                    new IllegalAccessException("Target directory contains directory with same name")
            );
        }
        log.traceExit();
    }

    private void moveDirectories(OperationArgumentsVO args, String error) throws DirectoryModificationException {
        log.traceEntry("{}", args);
        try {
            FileUtils.copyDirectory(args.currentPathOnDisk().toFile(), args.targetPathOnDisk().toFile());
            log.trace("Directory on disk was copied: '{}' -> '{}'", args.currentPathOnDisk(), args.targetPathOnDisk());
            args.source().setPath(args.targetPath());
            directoryJpaRepository.save(args.source());
            log.trace("Renamed source path on entity. New path: {}", args.source().getPath());
            replaceRelatedDirectoryPaths(args);

        } catch (IOException e) {
            log.debug("Exception while moving directory: {}", e);
            throw new DirectoryModificationException(error, e);
        }
        try {
            Files.walk(args.currentPathOnDisk())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("Failure delete {} after copy", path);
                        }
                    });
        } catch (IOException e) {
            log.error("Data was copied but source did not removed: {}", e);
        }

        log.traceExit();
    }

    private void replaceRelatedDirectoryPaths(OperationArgumentsVO args) {
        log.traceEntry("Replace related directory paths   {}", args.toString());
        List<Directory> directories = directoryJpaRepository
                .findByOwnerAndPathStartingWith(args.owner(), args.currentPath() + SEPARATOR);
        for (Directory dir : directories) {
            String pathAfterRename = dir.getPath().replaceFirst(args.currentPath, args.targetPath());
            log.trace("Change path: '{}' -> '{}'", dir.getPath(), pathAfterRename);
            dir.setPath(pathAfterRename);
        }
        log.debug("Paths was replaced with arguments = {}", args);
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
        return log.traceExit(directoryJpaRepository.findByOwnerAndPath(owner, path));
    }

    @Override
    public Optional<Directory> findById(Long directoryId) {
        return log.traceExit(directoryJpaRepository.findById(directoryId));
    }

    @Override
    public Optional<Directory> findByOwnerAndId(UserInfo owner, Long directoryId) {
        return log.traceExit(directoryJpaRepository.findById(directoryId));
    }

    private class OperationArgumentsVO {

        private final Directory source;
        private final UserInfo owner;
        private final String currentPath;
        private final String targetPath;

        OperationArgumentsVO(Directory source, String newParentPath, String newName) {
            this.source = source;
            currentPath = source.getPath();
            targetPath = buildPath(newParentPath, newName);
            owner = source.getOwner();
        }

        public Directory source() {
            return source;
        }

        public UserInfo owner() {
            return owner;
        }

        String targetPath() {
            return targetPath;
        }

        String fullTargetPath() {
            return foldersBaseDir + buildPath(owner.getLogin(), targetPath);
        }

        Path targetPathOnDisk() {
            return Paths.get(fullTargetPath());
        }

        String currentPath() {
            return currentPath;
        }

        public String fullCurrentPath() {
            return foldersBaseDir + buildPath(owner.getLogin(), currentPath);
        }

        Path currentPathOnDisk() {
            return Paths.get(fullCurrentPath());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OperationArgumentsVO that = (OperationArgumentsVO) o;
            return Objects.equals(source, that.source) &&
                    Objects.equals(owner, that.owner) &&
                    Objects.equals(currentPath, that.currentPath) &&
                    Objects.equals(targetPath, that.targetPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, owner, currentPath, targetPath);
        }

        @Override
        public String toString() {
            return "OperationArgumentsVO{" +
                    "source=" + source +
                    ", owner=" + owner.getLogin() +
                    ", currentPath='" + currentPath + '\'' +
                    ", targetPath='" + targetPath + '\'' +
                    '}';
        }
    }
}
