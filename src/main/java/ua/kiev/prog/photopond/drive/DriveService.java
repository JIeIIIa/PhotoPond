package ua.kiev.prog.photopond.drive;

import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.user.UserInfo;

public interface DriveService {
    long countByOwner(UserInfo owner);

    Content getDirectoryContent(String ownerLogin, String path) throws DriveException;

    Directory addDirectory(String ownerLogin, String parentDirectoryPath, String newDirectoryName) throws DriveException;

    void moveDirectory(String ownerLogin, String source, String target) throws DriveException;

    PictureFile addPictureFile(String ownerLogin, String directoryPath, MultipartFile multipartFile) throws DriveException;

    byte[] getFile(String ownerLogin, String path) throws DriveException;

    void deletePictureFile(String ownerLogin, String path) throws DriveException;

    void movePictureFile(String ownerLogin, String path, String newPath) throws DriveException;

    void deleteDirectory(String ownerLogin, String path) throws DriveException;
}
