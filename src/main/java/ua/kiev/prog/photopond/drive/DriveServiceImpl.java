package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kiev.prog.photopond.drive.directories.*;
import ua.kiev.prog.photopond.user.UserInfo;

import java.util.LinkedList;
import java.util.List;

@Service
public class DriveServiceImpl implements DriveService {
    private static final Logger log = LogManager.getLogger(DriveServiceImpl.class);

    private final DirectoryDiskAndDatabaseRepository directoryRepository;

    @Autowired
    public DriveServiceImpl(DirectoryDiskAndDatabaseRepository directoryRepository) {
        this.directoryRepository = directoryRepository;
    }

    @Override
    public long countByOwner(UserInfo owner) {
        log.traceEntry("Count directories by {}", owner);
        long count = directoryRepository.countByOwner(owner);
        return log.traceExit(count);
    }

    @Override
    public Content getDirectoryContent(UserInfo owner, String path) throws DriveException {
        log.traceEntry("Content at '{}' for {}", owner, path);

        createRootDirectoryIfNotExists(owner, path);

        List<Directory> currentList = directoryRepository.findByOwnerAndPath(owner, path);
        if (currentList.size() != 1) {
            log.debug("Failed to get directory for owner {} at path '{}'", owner, path);
            throw new DriveException("Not found or found more than one directory for " + owner + " at path " + path);
        }

        Directory current = currentList.get(0);
        Content content = getDirectoryContent(current);

        return content;
    }

    @Override
    public Directory addDirectory(UserInfo owner, Long parentDirectoryId, String newDirectoryName) throws DirectoryException {
        Directory directory = directoryRepository.findByOwnerAndId(owner, parentDirectoryId);
        if (directory == null) {
//            throw
        }
        String path = directory.getPath();
        if (!directory.isRoot()) {
            path += Directory.SEPARATOR;
        }
        path += newDirectoryName;

        Directory newDirectory = new DirectoryBuilder()
                .owner(owner)
                .path(path)
                .build();
        directoryRepository.save(newDirectory);

        return directory;
    }

    private Content getDirectoryContent(Directory current) throws DriveException {
        log.traceEntry("Get content for {}", current);
        Content content = new Content();
        content.setCurrentDirectory(current);

        List<Directory> parentDirectories = findParentDirectories(current);
        content.setParents(parentDirectories);

        List<Directory> topSubDirectories = directoryRepository.findTopSubDirectories(current);
        content.setTopSubDirectories(topSubDirectories);
        return log.traceExit(content);
    }

    private void createRootDirectoryIfNotExists(UserInfo owner, String path) throws DriveException {
        log.traceEntry("Check and try create root directory: owner={}   path='{}'", owner, path);
        if (!Directory.SEPARATOR.equals(path)) {
            log.trace("path='{}' is not root", path);
            return;
        }
        long count = directoryRepository.countByOwner(owner);
        if (count == 0) {
            Directory root = new DirectoryBuilder()
                    .owner(owner)
                    .path(Directory.SEPARATOR)
                    .build();
            try {
                log.debug("Try to create root directory for {}", owner);
                directoryRepository.save(root);
            } catch (DirectoryModificationException e) {
                log.debug("User {} has no content and failed to create root directory", owner);
                throw new DriveException("Failed to create root directory", e);
            }
        }
    }

    private List<Directory> findParentDirectories(Directory directory) throws DriveException {
        log.traceEntry("Find parent directories for '{}'", directory);
        List<Directory> list = new LinkedList<>();
        if (directory.isRoot()) {
            return list;
        }
        Directory current = directory;
        do {
            List<Directory> parents = directoryRepository.findByOwnerAndPath(current.getOwner(), current.getParentPath());
            if (parents.size() != 1) {
                log.debug("Failed to get parent directory for '{}'", current.getParentPath());
                throw new DriveException("Not found or found more than one parent directory for " + current);
            }
            current = parents.get(0);
            list.add(0, current);
            log.trace("Add parent directory '{}' in list", current);
        } while (!current.isRoot());
        return list;
    }


}
