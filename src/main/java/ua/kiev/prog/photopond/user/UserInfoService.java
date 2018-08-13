package ua.kiev.prog.photopond.user;

import java.util.List;
import java.util.Optional;

public interface UserInfoService {
    void addUser(UserInfo user);

    Optional<UserInfo> findUserByLogin(String login);

    List<UserInfo> findAllUsers();

    Optional<UserInfo> findById(long id);

    List<UserInfo> findAllByRole(UserRole role);

    boolean existsByLogin(String login);

    boolean existsWithAdminRole();

    Optional<UserInfo> delete(long id);

    Optional<UserInfo> update(UserInfo userInfo);

    boolean setNewPassword(UserInfoDTO userInfoDTO);

    boolean resetPassword(String login, String password);

    boolean updateAvatar(UserInfoDTO userInfoDTO);

    byte[] retrieveAvatar(String login);
}
