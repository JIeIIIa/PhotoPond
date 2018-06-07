package ua.kiev.prog.photopond.drive.directories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.kiev.prog.photopond.user.UserInfo;

import java.util.List;
import java.util.Optional;

public interface DirectoryJpaRepository extends JpaRepository<Directory, Long>{
    Optional<Directory> findById(Long id);

    List<Directory> findAll();

    Optional<Directory> findByOwnerAndId(UserInfo owner, Long id);

    List<Directory> findByOwnerAndPath(UserInfo owner, String path);

    List<Directory> findByOwnerAndPathStartingWith(UserInfo user, String pathPattern);

    List<Directory> findByOwnerAndPathStartingWithAndLevel(UserInfo owner, String pathPattern, Integer level);

    long countByOwner(UserInfo owner);
}
