package ua.kiev.prog.photopond.drive;

import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.user.UserInfo;

import java.util.Optional;

import static ua.kiev.prog.photopond.drive.directories.Directory.appendToPath;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

public class DriveItemDTOMapper {
    public static DriveItemDTO toDTO(Directory directory) {
        return toDTO(
                directory,
                buildPath("user",
                        Optional.of(directory)
                                .map(Directory::getOwner)
                                .map(UserInfo::getLogin)
                                .orElse(null),
                        "drive")
        );
    }

    public static DriveItemDTO toDTO(Directory directory, String baseUrl) {
        DriveItemDTO dto = new DriveItemDTO();
        dto.setName(directory.getName());
        dto.setParentUri(appendToPath(baseUrl, directory.parentPath()));
        dto.setType(DriveItemType.DIR);

        return dto;
    }

    public static DriveItemDTO toDTO(PictureFile file) {
        DriveItemDTO dto = new DriveItemDTO();
        dto.setName(file.getFilename());
        dto.setParentUri(buildPath("user",
                Optional.of(file)
                        .map(PictureFile::getDirectory)
                        .map(Directory::getOwner)
                        .map(UserInfo::getLogin)
                        .orElse(null),
                "files",
                file.parentPath()));
        dto.setType(DriveItemType.FILE);

        return dto;
    }
}
