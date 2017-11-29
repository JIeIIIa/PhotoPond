package ua.kiev.prog.photopond.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoJpaRepository extends JpaRepository<UserInfo, Long> {
    UserInfo findByLogin(String login);
}
