package ua.kiev.prog.photopond.user;

public interface UserInfoSimpleRepository {
    UserInfo findByLogin(String login);

    boolean existByLogin(String login);

    void addUser(UserInfo user);
}
