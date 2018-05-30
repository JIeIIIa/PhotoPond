package ua.kiev.prog.photopond.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoJpaRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByLogin(String login);

    Long countByLoginAndIdNot(String login, long id);
}
