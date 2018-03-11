package ua.kiev.prog.photopond.drive.pictures;

import ua.kiev.prog.photopond.drive.directories.Directory;

import java.util.List;

public interface PictureFileRepository {
    PictureFile findById(long id) throws PictureFileException;

    List<PictureFile> findByDirectory(Directory source) throws PictureFileException;

    List<PictureFile> findByDirectoryAndFilename(Directory source, String filename) throws PictureFileException;

    PictureFile save(PictureFile file) throws PictureFileException;

    void deleteByDirectoryAndFilename(Directory directory, String filename) throws PictureFileException;

    void move(Directory directory, String filename, Directory targetDirectory, String targetFilename) throws PictureFileException;
}
