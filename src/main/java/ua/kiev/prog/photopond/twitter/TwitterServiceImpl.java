package ua.kiev.prog.photopond.twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import ua.kiev.prog.photopond.Utils.Utils;
import ua.kiev.prog.photopond.drive.DriveException;
import ua.kiev.prog.photopond.drive.DriveService;
import ua.kiev.prog.photopond.facebook.Exception.AssociateFBAccountException;
import ua.kiev.prog.photopond.facebook.Exception.DisassociateFBAccountException;
import ua.kiev.prog.photopond.twitter.Exception.*;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;
import ua.kiev.prog.photopond.user.UserInfoMapper;

import java.io.ByteArrayInputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ua.kiev.prog.photopond.twitter.TwitterUtils.getTwitterInstance;

@Service
public class TwitterServiceImpl implements TwitterService {

    private static final Logger LOG = LogManager.getLogger(TwitterServiceImpl.class);

    private final UserInfoJpaRepository userInfoJpaRepository;

    private final TwitterRequestTokenStorage requestTokenStorage;
    private final TwitterUserJpaRepository twitterUserJpaRepository;
    private final DriveService driveService;

    @Autowired
    public TwitterServiceImpl(TwitterUserJpaRepository twitterUserJpaRepository,
                              UserInfoJpaRepository userInfoJpaRepository,
                              TwitterRequestTokenStorage requestTokenStorage,
                              DriveService driveService) {
        LOG.info("Create instance of {}", TwitterServiceImpl.class);
        this.twitterUserJpaRepository = twitterUserJpaRepository;
        this.userInfoJpaRepository = userInfoJpaRepository;
        this.requestTokenStorage = requestTokenStorage;
        this.driveService = driveService;
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
        Twitter twitter = getTwitterInstance();
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
            Twitter twitter = getTwitterInstance();
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
        Twitter twitter = getTwitterInstance();
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
    public TweetDTO publishTweet(String userLogin, TweetDTO tweetDTO) {
        TwitterUser twitterUser = userInfoJpaRepository.findByLogin(userLogin)
                .flatMap(twitterUserJpaRepository::findByUserInfo)
                .orElseThrow(() -> new NotFoundTwitterAssociatedAccountException("Not found Twitter associated account: userLogin = " + userLogin));
        List<byte[]> images = retrieveImagesData(userLogin, tweetDTO);
        try {
            Twitter twitter = getTwitterInstance(twitterUser);
            long[] ids = prepareImages(twitter, images);

            StatusUpdate update = new StatusUpdate(tweetDTO.getMessage());
            update.setMediaIds(ids);
            Status status = twitter.updateStatus(update);

            String url = "https://twitter.com/" + status.getUser().getScreenName() +
                    "/status/" + status.getId();
            tweetDTO.setUrl(url);

            LOG.debug("Tweet was created [userLogin = {}], [url = {}]", userLogin, url);
            return tweetDTO;
        } catch (TwitterException e) {
            throw new TweetPublishingException(e);
        }
    }

    private List<byte[]> retrieveImagesData(String userLogin, TweetDTO tweetDTO) {
        try {
            LOG.traceEntry("Retrieve images for tweet");

            return tweetDTO.getPaths().stream()
                    .map(p -> Utils.getUriTail(p, userLogin))
                    .map(p -> driveService.retrievePictureFileData(userLogin, p))
                    .collect(toList());
        } catch (DriveException e) {
            throw new TweetPublishingException(e);
        }
    }

    private long[] prepareImages(Twitter twitter, List<byte[]> images) throws TwitterException {
        long[] ids = new long[images.size()];
        LOG.trace("Send images to Twitter server");
        for (int i = 0; i < images.size(); i++) {
            UploadedMedia media = twitter.uploadMedia("filenameMock", new ByteArrayInputStream(images.get(i)));
            ids[i] = media.getMediaId();
        }

        LOG.debug("Images were sent");
        return ids;
    }

}
