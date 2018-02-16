package ua.kiev.prog.photopond.drive.directories;

import ua.kiev.prog.photopond.user.UserInfo;

import java.util.List;

public interface DirectoryDiskAndDatabaseRepository {
    void save(Directory directory) throws DirectoryModificationException;

    void delete(Directory directory) throws DirectoryModificationException;

    void rename(Directory directory, String newName) throws DirectoryModificationException;

    void move(Directory source, Directory target) throws DirectoryModificationException;

    List<Directory> findTopSubDirectories(Directory directory);

    long countByOwner(UserInfo owner);

    List<Directory> findByOwnerAndPath(UserInfo owner, String path);
}
