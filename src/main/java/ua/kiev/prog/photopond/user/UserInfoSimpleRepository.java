package ua.kiev.prog.photopond.user;

import ua.kiev.prog.photopond.exception.AddToRepositoryException;

public interface UserInfoSimpleRepository {
    UserInfo findByLogin(String login);

    boolean existByLogin(String login);

    void addUser(UserInfo user) throws AddToRepositoryException;
}
