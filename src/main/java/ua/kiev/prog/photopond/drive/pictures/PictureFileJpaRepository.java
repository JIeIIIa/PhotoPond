package ua.kiev.prog.photopond.drive.pictures;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.kiev.prog.photopond.drive.directories.Directory;

import java.util.List;
import java.util.Optional;

public interface PictureFileJpaRepository extends JpaRepository<PictureFile, Long> {
    Optional<PictureFile> findById(long id) throws PictureFileException;

    List<PictureFile> findByDirectory(Directory source);

    List<PictureFile> findByDirectoryAndFilename(Directory source, String filename);

    Optional<PictureFile> findFirstByDirectoryAndFilename(Directory source, String filename);

    Long deleteByDirectoryAndFilename(Directory source, String filename);
}
