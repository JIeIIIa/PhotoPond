package ua.kiev.prog.photopond.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.types.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kiev.prog.photopond.facebook.Exception.AssociateFBAccountException;
import ua.kiev.prog.photopond.facebook.Exception.DisassociateFBAccountException;
import ua.kiev.prog.photopond.facebook.Exception.FBAccountAlreadyAssociateException;
import ua.kiev.prog.photopond.facebook.Exception.FBAuthenticationException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;
import ua.kiev.prog.photopond.user.UserInfoMapper;

import java.util.Objects;
import java.util.Optional;

import static ua.kiev.prog.photopond.facebook.FBConstants.*;
import static ua.kiev.prog.photopond.facebook.FBUserMapper.toDto;

@Service
public class FBServiceImpl implements FBService {

    private static final Logger LOG = LogManager.getLogger(FBServiceImpl.class);

    private final FBUserJpaRepository fbUserJpaRepository;

    private final UserInfoJpaRepository userInfoJpaRepository;

    @Autowired
    public FBServiceImpl(FBUserJpaRepository fbUserJpaRepository, UserInfoJpaRepository userInfoJpaRepository) {
        LOG.info("Create instance of {}", FBServiceImpl.class);
        this.fbUserJpaRepository = fbUserJpaRepository;
        this.userInfoJpaRepository = userInfoJpaRepository;
    }

    @Transactional
    @Override
    public FBUserDTO associateAccount(String login, String code) {
        LOG.entry("Try to associate Facebook account with user [login = {}]", login);
        UserInfo userInfo = userInfoJpaRepository.findByLogin(login).orElseThrow(AssociateFBAccountException::new);

        fbUserJpaRepository.findByUserInfo(userInfo)
                .ifPresent(u -> {
                    throw new FBAccountAlreadyAssociateException("User [login = " + login + "] has already had associated Facebook account");
                });

        FBUser fbUser = createFBUser(userInfo, code);
        fbUserJpaRepository.saveAndFlush(fbUser);

        LOG.debug("Facebook account [id = {}, email = {}] was associated with user [login = {}]",
                fbUser.getFbId(), fbUser.getEmail(), userInfo.getLogin());
        return toDto(fbUser);
    }

    private FBUser createFBUser(UserInfo userInfo, String code) {
        LOG.trace("Start creating account for userInfo.id = {}", userInfo.getId());
        FBUser fbUser;
        try {
            FacebookClient.AccessToken accessToken = changeCodeToExtendedAccessToken(code);
            User user = retrieveUser(accessToken)
                    .orElseThrow(() -> new AssociateFBAccountException(""));
            if (fbUserJpaRepository.countByFbId(user.getId()) != 0) {
                throw new FBAccountAlreadyAssociateException("Facebook account [id = " + user.getId() + "has already associated");
            }

            fbUser = FBUserMapper.toEntity(user, accessToken, userInfo);
        } catch (FacebookException e) {
            throw new AssociateFBAccountException(e);
        }
        return LOG.traceExit(fbUser);
    }

    @Transactional(readOnly = true)
    @Override
    public UserInfoDTO findUserInfoByCode(String code) {
        LOG.trace("Try to find userInfo");
        FacebookClient.AccessToken accessToken = changeCodeToExtendedAccessToken(code);
        Optional<User> user = retrieveUser(accessToken);

        //todo: accessToken should be updated in database
        return user
                .flatMap(u -> fbUserJpaRepository.findByFbId(u.getId()))
                .map(FBUser::getUserInfo)
                .map(UserInfoMapper::toDto)
                .orElseThrow(() -> new FBAuthenticationException("Not found user information"));
    }

    @Transactional(readOnly = true)
    @Override
    public FBUserDTO findAccountByLogin(String login) {
        LOG.traceEntry("Find FBUser by Login = {}", login);
        return userInfoJpaRepository.findByLogin(login)
                .flatMap(fbUserJpaRepository::findByUserInfo)
                .map(FBUserMapper::toDto)
                .orElse(null);
    }

    @Transactional
    @Override
    public void disassociateAccount(String login) {
        LOG.traceEntry("Start disassociating account for user with login = {}", login);
        FBUser fbUser = userInfoJpaRepository.findByLogin(login)
                .flatMap(fbUserJpaRepository::findByUserInfo)
                .orElseThrow(() -> new DisassociateFBAccountException("Not found associated account information"));

        removeFacebookPermissions(fbUser);

        fbUserJpaRepository.deleteByUserInfo(fbUser.getUserInfo());
        LOG.traceEntry("Account for user with login = {} was disassociated", login);
    }

    private void removeFacebookPermissions(FBUser fbUser) {
        FacebookClient facebookClient = new DefaultFacebookClient(fbUser.getAccessToken(), FB_CLIENT_VERSION);
        facebookClient.deleteObject(fbUser.getFbId() + "/permissions");
        LOG.debug("Permissions for Facebook account [id = {}] was deleted", fbUser.getFbId());
    }

    private Optional<User> retrieveUser(FacebookClient.AccessToken accessToken) {
        if (Objects.isNull(accessToken)) {
            return Optional.empty();
        }
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), FB_CLIENT_VERSION);
        User me = facebookClient.fetchObject("/me", User.class, Parameter.with("fields", "email,name"));

        return Optional.of(me);
    }

    private FacebookClient.AccessToken changeCodeToExtendedAccessToken(String code) {
        FacebookClient facebookClient = new DefaultFacebookClient(FB_CLIENT_VERSION);
        FacebookClient.AccessToken userAccessToken = facebookClient
                .obtainUserAccessToken(getApplicationId(), getApplicationSecret(), FBConstants.getFullCallbackUrl(), code);

        FacebookClient.AccessToken extendedAccessToken = facebookClient.obtainExtendedAccessToken(getApplicationId(), getApplicationSecret(),
                userAccessToken.getAccessToken());
        LOG.trace("[Code = {} ] was exchanged to Extended access token", code);

        return extendedAccessToken;
    }
}
