package ua.kiev.prog.photopond.drive.directories;

import ua.kiev.prog.photopond.user.UserInfo;

import java.util.List;
import java.util.Optional;

public interface DirectoryDiskAndDatabaseRepository {
    Directory save(Directory directory) throws DirectoryModificationException;

    void delete(Directory directory) throws DirectoryModificationException;

    void rename(Directory directory, String newPath) throws DirectoryModificationException;

    void move(Directory source, Directory target) throws DirectoryModificationException;

    List<Directory> findTopSubDirectories(Directory directory);

    long countByOwner(UserInfo owner);

    List<Directory> findByOwnerAndPath(UserInfo owner, String path);

    Optional<Directory> findById(Long directoryId);

    Optional<Directory> findByOwnerAndId(UserInfo owner, Long directoryId);
}
