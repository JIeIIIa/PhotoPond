package ua.kiev.prog.photopond.Utils;

import org.apache.commons.io.FileUtils;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileJpaRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestUtils {
    public static void createDirectories(Path basedirPath, DirectoryJpaRepository directoryJpaRepository) throws IOException {
        FileUtils.deleteDirectory(basedirPath.toFile());
        List<Directory> directories = directoryJpaRepository.findAll();
        for (Directory dir : directories) {
            Path path = Paths.get(basedirPath + dir.getFullPath());
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createPictureFiles(Path basedirPath, PictureFileJpaRepository pictureFileJpaRepository) throws IOException {
        List<PictureFile> files = pictureFileJpaRepository.findAll();
        for (PictureFile file : files) {
            Path path = Paths.get(basedirPath + file.getFullPath());
            FileUtils.writeByteArrayToFile(path.toFile(), file.getFilename().getBytes(), false);
        }
    }
}
