package ua.kiev.prog.photopond.drive.pictures;

import ua.kiev.prog.photopond.drive.directories.Directory;

import java.util.List;
import java.util.Optional;

public interface PictureFileRepository {
    Optional<PictureFile> findById(Long id) throws PictureFileException;

    List<PictureFile> findByDirectory(Directory source) throws PictureFileException;

    List<PictureFile> findByDirectoryAndFilename(Directory source, String filename) throws PictureFileException;

    PictureFile save(PictureFile file) throws PictureFileException;

    void delete(PictureFile file) throws PictureFileException;

    void move(PictureFile file, Directory targetDirectory, String targetFilename) throws PictureFileException;
}
