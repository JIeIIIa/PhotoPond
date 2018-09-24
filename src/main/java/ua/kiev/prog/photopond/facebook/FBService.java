package ua.kiev.prog.photopond.facebook;

import ua.kiev.prog.photopond.user.UserInfoDTO;

public interface FBService {
    FBUserDTO associateAccount(String login, String code);

    UserInfoDTO findUserInfoByCode(String code);

    FBUserDTO findAccountByLogin(String login);

    void disassociateAccount(String login);
}
