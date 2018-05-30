package ua.kiev.prog.photopond.user;

import java.util.List;
import java.util.Optional;

public interface UserInfoService {
    Optional<UserInfo> getUserByLogin(String login);

    void addUser(UserInfo user);

    boolean existByLogin(String login);

    List<UserInfo> getAllUsers();

    UserInfo delete(long id);

    UserInfo update(UserInfo userInfo);

    Optional<UserInfo> getUserById(long id);
}
