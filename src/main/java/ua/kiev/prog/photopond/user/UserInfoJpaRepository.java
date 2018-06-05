package ua.kiev.prog.photopond.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInfoJpaRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByLogin(String login);

    List<UserInfo> findAllByRole(UserRole role);

    Long countByLoginAndIdNot(String login, long id);

    Long countByRole(UserRole role);

    void deleteById(Long id);
}
