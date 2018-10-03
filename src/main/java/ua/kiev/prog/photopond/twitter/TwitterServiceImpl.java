package ua.kiev.prog.photopond.twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import ua.kiev.prog.photopond.facebook.Exception.AssociateFBAccountException;
import ua.kiev.prog.photopond.facebook.Exception.DisassociateFBAccountException;
import ua.kiev.prog.photopond.twitter.Exception.AssociateTwitterAccountException;
import ua.kiev.prog.photopond.twitter.Exception.CustomTwitterException;
import ua.kiev.prog.photopond.twitter.Exception.TwitterAccountAlreadyAssociateException;
import ua.kiev.prog.photopond.twitter.Exception.TwitterAuthenticationException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;
import ua.kiev.prog.photopond.user.UserInfoMapper;

import java.io.File;

@Service
public class TwitterServiceImpl implements TwitterService {

    private static final Logger LOG = LogManager.getLogger(TwitterServiceImpl.class);

    private final UserInfoJpaRepository userInfoJpaRepository;

    private final TwitterRequestTokenStorage requestTokenStorage;
    private final TwitterUserJpaRepository twitterUserJpaRepository;

    @Autowired
    public TwitterServiceImpl(TwitterUserJpaRepository twitterUserJpaRepository,
                              UserInfoJpaRepository userInfoJpaRepository,
                              TwitterRequestTokenStorage requestTokenStorage) {
        LOG.info("Create instance of {}", TwitterServiceImpl.class);
        this.twitterUserJpaRepository = twitterUserJpaRepository;
        this.userInfoJpaRepository = userInfoJpaRepository;
        this.requestTokenStorage = requestTokenStorage;
    }

    @Override
    public void removeRequestToken(String requestToken) {
        LOG.traceEntry("Removed token = {}", requestToken);
        requestTokenStorage.retrieveAndRemoveRequestToken(requestToken);
    }

    @Override
    public String getAuthorizationUrl() {
        LOG.traceEntry();
        try {
            RequestToken requestToken = generateRequestToken(TwitterConstants.getAssociateCallbackUrl());
            return LOG.traceExit("AuthorizationURL = {}", requestToken.getAuthorizationURL());
        } catch (TwitterException e) {
            throw new AssociateTwitterAccountException(e);
        }
    }

    @Override
    public String getAuthenticationUrl() {
        LOG.traceEntry();
        try {
            RequestToken requestToken = generateRequestToken(TwitterConstants.getLoginCallbackUrl());
            return LOG.traceExit("AuthenticationURL = {}", requestToken.getAuthenticationURL());
        } catch (TwitterException e) {
            throw new TwitterAuthenticationException(e);
        }
    }

    private RequestToken generateRequestToken(String callbackUrl) throws TwitterException {
        Twitter twitter = TwitterUtils.getTwitterInstance();
        RequestToken requestToken = twitter.getOAuthRequestToken(callbackUrl);
        requestTokenStorage.add(requestToken);
        return requestToken;
    }

    @Transactional
    @Override
    public TwitterUserDTO associateAccount(String login, String oauthToken, String oauthVerifier) {
        LOG.entry("Try to associate Twitter account with user [login = {}]", login);
        UserInfo userInfo = userInfoJpaRepository.findByLogin(login).orElseThrow(AssociateFBAccountException::new);

        twitterUserJpaRepository.findByUserInfo(userInfo)
                .ifPresent(u -> {
                    throw new TwitterAccountAlreadyAssociateException("User [login = " + login + "] has already had associated Twitter account");
                });

        TwitterUser twitterUser = createTwitterUser(userInfo, oauthToken, oauthVerifier);
        twitterUserJpaRepository.saveAndFlush(twitterUser);

        LOG.debug("Twitter account [id = {}, name = {}] was associated with user [login = {}]",
                twitterUser.getSocialId(), twitterUser.getName(), userInfo.getLogin());
        return TwitterUserMapper.toDto(twitterUser);
    }

    private TwitterUser createTwitterUser(UserInfo userInfo, String oauthToken, String oauthVerifier) {
        LOG.trace("Start creating account for userInfo.id = {}", userInfo.getId());
        TwitterUser twitterUser;
        try {
            RequestToken requestToken = requestTokenStorage
                    .retrieveAndRemoveRequestToken(oauthToken)
                    .orElseThrow(AssociateTwitterAccountException::new);
            Twitter twitter = TwitterUtils.getTwitterInstance();
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);

            User user = twitter.verifyCredentials();

            twitterUserJpaRepository.findBySocialId(user.getId())
                    .ifPresent(u -> {
                        throw new TwitterAccountAlreadyAssociateException("Twitter account [" +
                                "id = " + user.getId() +
                                ";  name = " + user.getScreenName() +
                                "] has already had associated with another profile");
                    });

            twitterUser = TwitterUserMapper.toEntity(user, accessToken, userInfo);
            twitterUserJpaRepository.saveAndFlush(twitterUser);
        } catch (TwitterException e) {
            throw new AssociateTwitterAccountException(e);
        }
        return LOG.traceExit(twitterUser);
    }

    @Transactional
    @Override
    public UserInfoDTO findUserInfoByRequestToken(String token, String verifier) {
        LOG.trace("Try to find userInfo");
        Twitter twitter = TwitterUtils.getTwitterInstance();
        try {
            RequestToken requestToken = requestTokenStorage
                    .retrieveAndRemoveRequestToken(token)
                    .orElseThrow(TwitterAuthenticationException::new);
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
            User user = twitter.verifyCredentials();
            LOG.debug("Retrieved information: user = [id = {}; screenName = {}]", user.getId(), user.getScreenName());
            return twitterUserJpaRepository.findBySocialId(user.getId())
                    .map(u -> updateTwitterUserToken(accessToken, u))
                    .map(TwitterUser::getUserInfo)
                    .map(UserInfoMapper::toDto)
                    .orElseThrow(TwitterAuthenticationException::new);
        } catch (TwitterException e) {
            throw new TwitterAuthenticationException(e);
        }
    }

    private TwitterUser updateTwitterUserToken(AccessToken accessToken, TwitterUser twitterUser) {
        twitterUser.setToken(accessToken.getToken());
        twitterUser.setTokenSecret(accessToken.getTokenSecret());
        return twitterUserJpaRepository.saveAndFlush(twitterUser);
    }

    @Transactional(readOnly = true)
    @Override
    public TwitterUserDTO findAccountByLogin(String login) {
        LOG.traceEntry("Find TwitterUser by Login = {}", login);
        return userInfoJpaRepository.findByLogin(login)
                .flatMap(twitterUserJpaRepository::findByUserInfo)
                .map(TwitterUserMapper::toDto)
                .orElse(null);
    }

    @Transactional
    @Override
    public void disassociateAccount(String login) {
        LOG.traceEntry("Start disassociating account for user with login = {}", login);
        TwitterUser twitterUser = userInfoJpaRepository.findByLogin(login)
                .flatMap(twitterUserJpaRepository::findByUserInfo)
                .orElseThrow(() -> new DisassociateFBAccountException("Not found associated account information"));

        twitterUserJpaRepository.deleteByUserInfo(twitterUser.getUserInfo());
        LOG.traceEntry("Account for user with login = {} was disassociated", login);
    }

    @Transactional(readOnly = true)
    @Override
    public String testPostPicture(String login) {
        TwitterUser twitterUser = userInfoJpaRepository.findByLogin(login)
                .flatMap(twitterUserJpaRepository::findByUserInfo)
                .orElseThrow(() -> new CustomTwitterException(""));

        try {
            Twitter twitter = TwitterUtils.getTwitterInstance(new AccessToken(twitterUser.getToken(), twitterUser.getTokenSecret()));
            User user = twitter.verifyCredentials();

            if (user.getId() != twitterUser.getSocialId()) {
                //todo: should be another exception
                throw new CustomTwitterException("");
            }
            //todo: change code below
            long[] ids = new long[2];
            String statusTxt = "Checkout my new image";

            File photo1 = new File("C:\\Users\\JIeIIIa\\Downloads\\150_150_1_799bad553f97d5744a40.jpg");
            UploadedMedia media = twitter.uploadMedia(photo1);
            ids[0] = media.getMediaId();

            File photo2 = new File("C:\\Users\\JIeIIIa\\Downloads\\17982968-Tasteful-Chocolate-Eclair-isolated-on-white-background-Stock-Photo.jpg");
            UploadedMedia media2 = twitter.uploadMedia(photo2);
            ids[1] = media2.getMediaId();

            //uploader.upload(photo, status);
            StatusUpdate update = new StatusUpdate("sharing 2 photos");
            update.setMediaIds(ids);
            Status status = twitter.updateStatus(update);

            System.out.println("Status was updated [" + status.getText() + "]");
            return "Status was updated [" + status.getText() + "]"
                    + "<br> <a href=https://twitter.com/" + status.getUser().getScreenName()
                    + "/status/" + status.getId() + ">link to tweet</a>";
        } catch (TwitterException e) {
            //todo: should be another exception
            throw new CustomTwitterException("test posting error");
        }
    }


}
