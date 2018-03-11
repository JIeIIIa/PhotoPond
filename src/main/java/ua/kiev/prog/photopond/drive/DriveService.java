package ua.kiev.prog.photopond.drive;

import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryException;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.user.UserInfo;

public interface DriveService {
//    List<Directory> findTopSubdirectories(UserInfo owner, Directory directory);

    long countByOwner(UserInfo owner);

    Content getDirectoryContent(String ownerLogin, String path) throws DriveException;

    Directory addDirectory(UserInfo owner, Long parentDirectoryId, String newDirectoryName) throws DirectoryException;

    PictureFile addPictureFile(String ownerLogin, String directoryPath, MultipartFile multipartFile) throws DriveException;

    byte[] getFile(String ownerLogin, String path) throws DriveException;

    void deletePictureFile(String ownerLogin, String path) throws DriveException;

    void movePictureFile(String ownerLogin, String path, String newPath) throws DriveException;
}
