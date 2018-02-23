package ua.kiev.prog.photopond.drive;

import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryException;
import ua.kiev.prog.photopond.user.UserInfo;

public interface DriveService {
//    List<Directory> findTopSubdirectories(UserInfo owner, Directory directory);

    long countByOwner(UserInfo owner);

    Content getDirectoryContent(UserInfo owner, String path) throws DriveException;

    Directory addDirectory(UserInfo owner, Long parentDirectoryId, String newDirectoryName) throws DirectoryException;
}
