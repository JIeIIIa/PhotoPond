package ua.kiev.prog.photopond.user;

import java.util.List;

public interface UserInfoService {
    UserInfo getUserByLogin(String login);

    void addUser(UserInfo user);

    boolean existByLogin(String login);

    List<UserInfo> getAllUsers();

    UserInfo delete(long id);

    UserInfo update(UserInfo userInfo);

    UserInfo getUserById(long id);
}
