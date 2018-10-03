package ua.kiev.prog.photopond.twitter;

import ua.kiev.prog.photopond.user.UserInfoDTO;

public interface TwitterService {
    void removeRequestToken(String requestToken);

    String getAuthorizationUrl();

    String getAuthenticationUrl();

    TwitterUserDTO associateAccount(String login, String oauthToken, String oauthVerifier);

    UserInfoDTO findUserInfoByRequestToken(String token, String verifier);

    TwitterUserDTO findAccountByLogin(String login);

    void disassociateAccount(String login);

    String testPostPicture(String login);
}
