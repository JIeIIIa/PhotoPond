package ua.kiev.prog.photopond.twitter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.user.UserInfo;

import java.util.Optional;

@Repository
public interface TwitterUserJpaRepository extends JpaRepository<TwitterUser, Long> {
    Optional<TwitterUser> findByUserInfo(UserInfo userInfo);

    Optional<TwitterUser> findBySocialId(Long socialId);

    void deleteByUserInfo(UserInfo userInfo);
}
