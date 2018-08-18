package ua.kiev.prog.photopond.user;

import java.util.List;
import java.util.Optional;

public interface UserInfoService {
    void addUser(UserInfoDTO user);

    Optional<UserInfoDTO> findUserByLogin(String login);

    List<UserInfoDTO> findAllUsers();

    Optional<UserInfoDTO> findById(long id);

    List<UserInfoDTO> findAllByRole(UserRole role);

    boolean existsByLogin(String login);

    boolean existsWithAdminRole();

    Optional<UserInfo> delete(long id);

    Optional<UserInfo> update(UserInfo userInfo);

    boolean setNewPassword(UserInfoDTO userInfoDTO);

    boolean resetPassword(String login, String password);

    boolean updateAvatar(UserInfoDTO userInfoDTO);

    byte[] retrieveAvatar(String login);
}
