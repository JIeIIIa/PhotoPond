package ua.kiev.prog.photopond.drive.directories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.kiev.prog.photopond.user.UserInfo;

import java.util.List;

public interface DirectoryJpaRepository extends JpaRepository<Directory, Long>{
    Directory findById(Long id);

    List<Directory> findAll();

    List<Directory> findByOwnerAndPath(UserInfo owner, String path);

    List<Directory> findByOwnerAndPathStartingWith(UserInfo user, String pathPattern);

    List<Directory> findByOwnerAndPathStartingWithAndLevel(UserInfo owner, String path, Integer level);

    @Modifying
    @Query(value = "UPDATE Directories SET path = REPLACE(path, ?1, ?2) WHERE  "
            + "owner_id = ?3", nativeQuery = true)
    int rename(@Param("oldPath") String oldPath, @Param("targetPath") String targetPath, @Param("ownerId") long ownerId);
}
