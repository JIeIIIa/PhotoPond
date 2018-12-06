package ua.kiev.prog.photopond.drive.pictures;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PictureFileDataJpaRepository extends JpaRepository<PictureFileData, Long> {
    Optional<PictureFileData> findByPictureFile(PictureFile pictureFile);
}
