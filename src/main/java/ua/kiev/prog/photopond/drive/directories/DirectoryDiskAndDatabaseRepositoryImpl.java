package ua.kiev.prog.photopond.drive.directories;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.user.UserInfo;

import javax.persistence.TransactionRequiredException;
import java.io.File;
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
    private static final Logger LOG = LogManager.getLogger(DirectoryDiskAndDatabaseRepositoryImpl.class);

    @Value("${folders.basedir.location}")
    private String foldersBaseDir;

    private final DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    public DirectoryDiskAndDatabaseRepositoryImpl(DirectoryJpaRepository directoryJpaRepository) {
        LOG.info("Create instance of {} with parameter {}", DirectoryDiskAndDatabaseRepositoryImpl.class, directoryJpaRepository);
        this.directoryJpaRepository = directoryJpaRepository;
    }

    void setFoldersBasedir(String foldersBaseDir) {
        this.foldersBaseDir = foldersBaseDir;
    }

    private void throwExceptionIfDirectoryNull(Directory directory) {
        if (directory == null) {
            LOG.debug("Directory:   NULL");
            throw new IllegalArgumentException("directory is null");
        }
    }

    @Override
    public Directory save(Directory directory) throws DirectoryModificationException {
        LOG.traceEntry("Save {}", directory);
        throwExceptionIfDirectoryNull(directory);

        if (directory.getLevel() > 1 &&
                directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.parentPath()).size() != 1) {
            LOG.debug("Not exists parent directory for current saved directory {}", directory);
            throw new DirectoryModificationException("Not exists parent directory for current saved directory");
        }

        directory = directoryJpaRepository.save(directory);
        Path pathOnDisk = Paths.get(foldersBaseDir + directory.getFullPath());
        if (Files.notExists(pathOnDisk, LinkOption.NOFOLLOW_LINKS)) {
            LOG.trace("Try to create directory:   {}", pathOnDisk);
            try {
                Files.createDirectories(pathOnDisk);
            } catch (IOException e) {
                LOG.error("Cannot create directory on disk:   {}", pathOnDisk);
                throw new DirectoryModificationException("Cannot create directory on disk", e);
            }
            LOG.debug("Directory was created on disk:   {}", pathOnDisk);
        }
        return LOG.traceExit("Directory was saved in database:   {}", directory);
    }

    @Override
    public void delete(Directory directory) throws DirectoryModificationException {
        LOG.traceEntry("Delete {}", directory);
        throwExceptionIfDirectoryNull(directory);

        deleteFromDatabase(directory);
        deleteFromDisk(directory);
        LOG.traceExit();
    }

    private void deleteFromDatabase(Directory directory) throws DirectoryModificationException {
        try {
            List<Directory> directoriesToDelete = directoryJpaRepository.findByOwnerAndPathStartingWith(
                    directory.getOwner(), directory.getPath() + SEPARATOR
            );
            directoriesToDelete.add(directory);
            directoryJpaRepository.deleteAll(directoriesToDelete);
            LOG.trace("Directory with contents was deleted from database:   {}", directory);
        } catch (TransactionRequiredException | DataAccessException | IllegalArgumentException e) {
            LOG.warn("Failure deleting from jpa repository for '{}'", directory);
            throw new DirectoryModificationException("Failure deleting from jpa repository: " + directory, e);
        }
    }

    private void deleteFromDisk(Directory directory) {
        try {
            FileUtils.deleteDirectory(new File(foldersBaseDir + directory.getFullPath()));
            LOG.trace("Directory with contents was deleted from disk:   {}", directory);
        } catch (IOException | IllegalArgumentException e) {
            LOG.error("Failure deleting from disk for '{}'", directory);
        }
    }

    @Override
    public void rename(Directory directory, String newPath) throws DirectoryModificationException {
        LOG.traceEntry("Rename:   {}   ->   {}", directory, newPath);
        throwExceptionIfDirectoryNull(directory);

        String newName = Directory.getName(newPath);
        if (Objects.equals(directory.getName(), newName)) {
            LOG.traceExit("New and old names are same:  {}", directory);
            return;
        }

        OperationArgumentsVO parameters = new OperationArgumentsVO(directory, retrieveParentPath(newPath), newName);

        isPossibleToRename(parameters);
        moveDirectories(parameters, "Rename: cannot rename directory on disk");
        LOG.traceExit();
    }

    private void isPossibleToRename(OperationArgumentsVO parameters) throws DirectoryModificationException {
        if (!Files.exists(parameters.currentPathOnDisk())) {
            LOG.debug("Rename:   directory with name '{}' not found on disk", parameters.currentPathOnDisk());
            throw new DirectoryModificationException("Cannot rename directory",
                    new IllegalAccessException("Directory with name '" + parameters.currentPathOnDisk() + "' not found on disk")
            );
        }
        if (directoryJpaRepository.findByOwnerAndPath(parameters.owner(), parameters.targetPath()).size() > 0) {
            LOG.debug("Rename:   directory on path '{}' already exists in database for user {}", parameters.targetPath(), parameters.owner.getLogin());
            throw new DirectoryModificationException("Cannot rename directory",
                    new IllegalAccessException("Directory on path '" + parameters.targetPath() + "' already exists in database for user '" + parameters.owner.getLogin() + "'")
            );
        }
        String parentDirectoryPath = retrieveParentPath(parameters.targetPath());
        if (directoryJpaRepository.findByOwnerAndPath(parameters.owner(), parentDirectoryPath).isEmpty()) {
            LOG.debug("Rename:   not found parent directory '{}'", parentDirectoryPath);
            throw new DirectoryModificationException("Cannot rename directory",
                    new IllegalAccessException("Not found parent directory '" + parentDirectoryPath + "'")
            );
        }
        LOG.traceExit();
    }

    @Override
    public void move(Directory source, Directory target) throws DirectoryModificationException {
        LOG.traceEntry("Move directory:   " + source);
        throwExceptionIfDirectoryNull(source);
        throwExceptionIfDirectoryNull(target);

        if (source.equals(target)) {
            LOG.traceExit("Source and target are equal:  {}", source);
            return;
        }

        isPossibleToMove(source, target);
        OperationArgumentsVO parameters = new OperationArgumentsVO(source, target.getPath(), source.getName());

        moveDirectories(parameters, "Move: cannot move directory or subdirectories on disk");
        LOG.traceExit();
    }

    private void isPossibleToMove(Directory source, Directory target) throws DirectoryModificationException {
        LOG.traceEntry();
        if (!source.getOwner().equals(target.getOwner())) {
            LOG.debug("Source and target directories must have same owner!   source={},   Target={}", source, target);
            throw new DirectoryModificationException("Cannot move directory",
                    new IllegalAccessException("Source and target directories must have same owner")
            );
        }
        if (target.getPath().startsWith(source.getPath())) {
            LOG.debug("Target [{}] is subdirectory fot [{}]", target.getPath(), source.getPath());
            throw new DirectoryModificationException("Cannot move directory",
                    new IllegalAccessException("Target [" + target.getPath() + "]directory is subdirectory for [" + source.getPath() + "]")
            );
        }
        if (!directoryJpaRepository.findByOwnerAndPath(source.getOwner(), target.getPath() + SEPARATOR + source.getName()).isEmpty()) {
            LOG.debug("Target [{}] contains directory with same name='{}'", target, source.getName());
            throw new DirectoryModificationException("Cannot move directory",
                    new IllegalAccessException("Target directory contains directory with same name")
            );
        }
        LOG.traceExit();
    }

    private void moveDirectories(OperationArgumentsVO args, String error) throws DirectoryModificationException {
        LOG.traceEntry("{}", args);
        try {
            FileUtils.copyDirectory(args.currentPathOnDisk().toFile(), args.targetPathOnDisk().toFile());
        } catch (IOException e) {
            LOG.debug("Exception while moving directory: {}", e);
            throw new DirectoryModificationException(error, e);
        }
        LOG.trace("Directory on disk was copied: '{}' -> '{}'", args.currentPathOnDisk(), args.targetPathOnDisk());

        moveDirectoriesOnDatabase(args);
        moveDirectoriesOnDisk(args);

        LOG.traceExit();
    }

    private void moveDirectoriesOnDatabase(OperationArgumentsVO args) {
        args.source().setPath(args.targetPath());
        directoryJpaRepository.save(args.source());
        LOG.trace("Renamed source path on entity. New path: {}", args.source().getPath());
        replaceRelatedDirectoryPaths(args);

    }

    private void replaceRelatedDirectoryPaths(OperationArgumentsVO args) {
        LOG.traceEntry("Replace related directory paths   {}", args.toString());
        List<Directory> directories = directoryJpaRepository
                .findByOwnerAndPathStartingWith(args.owner(), args.currentPath() + SEPARATOR);
        for (Directory dir : directories) {
            String pathAfterRename = dir.getPath().replaceFirst(args.currentPath, args.targetPath());
            LOG.trace("Change path: '{}' -> '{}'", dir.getPath(), pathAfterRename);
            dir.setPath(pathAfterRename);
            directoryJpaRepository.save(dir);
        }
        LOG.debug("Paths was replaced with arguments = {}", args);
    }

    private void moveDirectoriesOnDisk(OperationArgumentsVO args) {
        try {
            Files.walk(args.currentPathOnDisk())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            LOG.error("Failure delete {} after copy", path);
                        }
                    });
        } catch (IOException e) {
            LOG.error("Data was copied but source did not removed: {}", e);
        }
    }

    @Override
    public List<Directory> findTopLevelSubDirectories(Directory directory) {
        LOG.traceEntry("Directory:   " + directory);
        throwExceptionIfDirectoryNull(directory);
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
        LOG.debug("Count top-level subdirectories = {}      for {}", topSubDirectories.size(), directory);
        return LOG.traceExit(topSubDirectories);
    }

    @Override
    public long countByOwner(UserInfo owner) {
        LOG.traceEntry("Count by {}", owner);
        long count = directoryJpaRepository.countByOwner(owner);

        return LOG.traceExit(count);
    }

    @Override
    public List<Directory> findByOwnerAndPath(UserInfo owner, String path) {
        LOG.traceEntry("Find by owner = {} and path = '{}'", owner.getLogin(), path);
        return LOG.traceExit(directoryJpaRepository.findByOwnerAndPath(owner, path));
    }

    @Override
    public Optional<Directory> findById(Long directoryId) {
        return LOG.traceExit(directoryJpaRepository.findById(directoryId));
    }

    @Override
    public Optional<Directory> findByOwnerAndId(UserInfo owner, Long directoryId) {
        return LOG.traceExit(directoryJpaRepository.findByOwnerAndId(owner, directoryId));
    }

    private class OperationArgumentsVO {

        private final Directory source;
        private final UserInfo owner;
        private final String currentPath;
        private final String targetPath;

        OperationArgumentsVO(Directory source, String newParentPath, String newName) {
            this.source = source;
            this.owner = source.getOwner();
            this.currentPath = source.getPath();
            this.targetPath = buildPath(newParentPath, newName);
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
