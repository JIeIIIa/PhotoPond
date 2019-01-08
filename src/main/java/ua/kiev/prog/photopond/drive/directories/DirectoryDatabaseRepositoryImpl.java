package ua.kiev.prog.photopond.drive.directories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.annotation.profile.DatabaseStorage;
import ua.kiev.prog.photopond.drive.exception.DirectoryModificationException;
import ua.kiev.prog.photopond.user.UserInfo;

import javax.persistence.TransactionRequiredException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ua.kiev.prog.photopond.drive.directories.Directory.*;

@SuppressWarnings("Duplicates")
@Repository
@DatabaseStorage
public class DirectoryDatabaseRepositoryImpl implements DirectoryRepository {
    private static final Logger LOG = LogManager.getLogger(DirectoryDatabaseRepositoryImpl.class);

    private final DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    public DirectoryDatabaseRepositoryImpl(DirectoryJpaRepository directoryJpaRepository) {
        LOG.info("Create instance of {} with parameter {}", DirectoryDatabaseRepositoryImpl.class, directoryJpaRepository);
        this.directoryJpaRepository = directoryJpaRepository;
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

        return LOG.traceExit("Directory was saved in database:   {}", directory);
    }

    @Override
    public void delete(Directory directory) throws DirectoryModificationException {
        LOG.traceEntry("Delete {}", directory);
        throwExceptionIfDirectoryNull(directory);

        deleteFromDatabase(directory);
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

        moveDirectoriesOnDatabase(args);

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
    public List<Directory> findByOwnerAndPathStartingWith(UserInfo owner, String pathPattern) {
        LOG.traceEntry("Find by owner = {} and path starting with = '{}'", owner.getLogin(), pathPattern);
        return LOG.traceExit(directoryJpaRepository.findByOwnerAndPathStartingWith(owner, pathPattern));
    }

    @Override
    public Optional<Directory> findById(Long directoryId) {
        return LOG.traceExit(directoryJpaRepository.findById(directoryId));
    }

    @Override
    public Optional<Directory> findByOwnerAndId(UserInfo owner, Long directoryId) {
        return LOG.traceExit(directoryJpaRepository.findByOwnerAndId(owner, directoryId));
    }

    @Override
    public boolean exists(UserInfo owner, String path) {
        return LOG.traceExit(
                directoryJpaRepository.findByOwnerAndPath(owner, path).size() > 0
        );
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

        String currentPath() {
            return currentPath;
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
