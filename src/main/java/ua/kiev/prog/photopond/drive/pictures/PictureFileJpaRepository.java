package ua.kiev.prog.photopond.drive.pictures;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.kiev.prog.photopond.drive.directories.Directory;

import java.util.List;

public interface PictureFileJpaRepository extends JpaRepository<PictureFile, Long> {
    PictureFile findById(long id) throws PictureFileException;

    List<PictureFile> findByDirectory(Directory source);

    List<PictureFile> findByDirectoryAndFilename(Directory source, String filename);

    PictureFile findFirstByDirectoryAndFilename(Directory source, String filename);

    Long removeByDirectoryAndFilename(Directory source, String filename);
}
