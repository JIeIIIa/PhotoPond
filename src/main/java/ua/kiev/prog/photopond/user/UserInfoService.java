package ua.kiev.prog.photopond.user;

public interface UserInfoService {
    UserInfo getUserByLogin(String login);

    void addUser(UserInfo user);

    boolean existByLogin(String login);
}
