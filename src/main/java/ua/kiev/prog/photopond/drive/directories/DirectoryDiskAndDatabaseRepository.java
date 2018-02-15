package ua.kiev.prog.photopond.drive.directories;

import java.util.List;

public interface DirectoryDiskAndDatabaseRepository {
    void save(Directory directory) throws DirectoryModificationException;

    void delete(Directory directory) throws DirectoryModificationException;

    void rename(Directory directory, String newName) throws DirectoryModificationException;

    void move(Directory source, Directory target) throws DirectoryModificationException;

    List<Directory> findTopSubDirectories(Directory directory);
}
