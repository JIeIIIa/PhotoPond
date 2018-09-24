package ua.kiev.prog.photopond.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.user.UserInfo;

import java.util.Optional;

@Repository
public interface FBUserJpaRepository extends JpaRepository<FBUser, Long> {

    Optional<FBUser> findByUserInfo(UserInfo userInfo);

    void deleteByUserInfo(UserInfo userInfo);

    Long countByFbId(String fbId);

    Optional<FBUser> findByFbId(String id);
}
