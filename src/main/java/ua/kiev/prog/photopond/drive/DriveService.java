package ua.kiev.prog.photopond.drive;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DriveService {
    List<DriveItemDTO> retrieveContent(String ownerLogin, String path, boolean withFiles) throws DriveException;

    DirectoriesDTO retrieveDirectories(String ownerLogin, String path) throws DriveException;

    DriveItemDTO addDirectory(String ownerLogin, String parentDirectoryPath, String newDirectoryName) throws DriveException;

    void moveDirectory(String ownerLogin, String source, String target) throws DriveException;

    DriveItemDTO addPictureFile(String ownerLogin, String directoryPath, MultipartFile multipartFile) throws DriveException;

    byte[] retrievePictureFileData(String ownerLogin, String path) throws DriveException;

    void deletePictureFile(String ownerLogin, String path) throws DriveException;

    void movePictureFile(String ownerLogin, String path, String newPath) throws DriveException;

    void deleteDirectory(String ownerLogin, String path) throws DriveException;
}
