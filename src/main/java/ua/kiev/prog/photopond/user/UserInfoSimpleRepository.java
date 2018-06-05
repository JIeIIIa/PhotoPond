package ua.kiev.prog.photopond.user;

import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.List;
import java.util.Optional;

public interface UserInfoSimpleRepository {
    void addUser(UserInfo user) throws AddToRepositoryException;

    Optional<UserInfo> findUserByLogin(String login);

    List<UserInfo> findAllUsers();

    Optional<UserInfo> findById(long id);

    List<UserInfo> findAllByRole(UserRole role);

    boolean existsByLogin(String login);

    boolean existsByLogin(String login, long exceptId);

    void delete(long id);

    UserInfo update(UserInfo userInfo);

    Long countByRole(UserRole role);
}
