package ua.kiev.prog.photopond.twitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import ua.kiev.prog.photopond.drive.DriveService;
import ua.kiev.prog.photopond.drive.exception.DriveException;
import ua.kiev.prog.photopond.twitter.Exception.*;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.user.UserInfoMapper.toDto;
import static ua.kiev.prog.photopond.user.UserRole.ADMIN;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TwitterConstantsConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class TwitterServiceImplTest {
    @MockBean
    private UserInfoJpaRepository userInfoJpaRepository;
    @MockBean
    private TwitterRequestTokenStorage requestTokenStorage;
    @MockBean
    private DriveService driveService;
    @MockBean
    private TwitterUserJpaRepository twitterUserJpaRepository;
    @MockBean
    private TwitterFactoryFacade twitterFactory;

    private Twitter twitter;

    private TwitterServiceImpl instance;

    @BeforeEach
    void setUp() {
        reset(twitterUserJpaRepository, userInfoJpaRepository, requestTokenStorage, driveService, twitterFactory);
        twitter = mock(Twitter.class);

        instance = new TwitterServiceImpl(twitterUserJpaRepository, userInfoJpaRepository, requestTokenStorage,
                driveService, twitterFactory);

        when(twitterFactory.getTwitterInstance()).thenReturn(twitter);
    }

    @Test
    void removeRequestToken() {
        //Given
        String requestToken = "request-token";

        //When
        instance.removeRequestToken(requestToken);

        //Then
        verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(requestToken);
        verifyNoMoreInteractions(twitterUserJpaRepository, userInfoJpaRepository, requestTokenStorage,
                driveService, twitterFactory);
    }

    @Nested
    class GetAuthorizationUrl {
        @Test
        void success() throws TwitterException {
            //Given
            final RequestToken expectedToken = new RequestToken("token", "token-secret");
            when(twitter.getOAuthRequestToken(any())).thenReturn(expectedToken);

            //When
            String result = instance.getAuthorizationUrl();

            //Then
            assertThat(result).isEqualTo(expectedToken.getAuthorizationURL());
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthRequestToken(any());
            verify(requestTokenStorage, times(1)).add(expectedToken);
            verifyNoMoreInteractions(userInfoJpaRepository, requestTokenStorage, twitterFactory);
            verifyZeroInteractions(twitterUserJpaRepository, driveService);
        }

        @Test
        void withException() throws TwitterException {
            //Given
            when(twitter.getOAuthRequestToken(any())).thenThrow(TwitterException.class);

            //When
            assertThrows(AssociateTwitterAccountException.class, () -> instance.getAuthorizationUrl());

            //Then
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthRequestToken(any());
            verifyNoMoreInteractions(userInfoJpaRepository, twitterFactory);
            verifyZeroInteractions(twitterUserJpaRepository, requestTokenStorage, driveService);
        }
    }

    @Nested
    class GetAuthenticationUrl {
        @Test
        void success() throws TwitterException {
            //Given
            final RequestToken expectedToken = new RequestToken("token", "token-secret");
            when(twitter.getOAuthRequestToken(any())).thenReturn(expectedToken);

            //When
            String result = instance.getAuthenticationUrl();

            //Then
            assertThat(result).isEqualTo(expectedToken.getAuthenticationURL());
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthRequestToken(any());
            verify(requestTokenStorage, times(1)).add(expectedToken);
            verifyNoMoreInteractions(userInfoJpaRepository, requestTokenStorage, twitterFactory);
            verifyZeroInteractions(twitterUserJpaRepository, driveService);
        }

        @Test
        void withException() throws TwitterException {
            //Given
            when(twitter.getOAuthRequestToken(any())).thenThrow(TwitterException.class);

            //When
            assertThrows(TwitterAuthenticationException.class, () -> instance.getAuthenticationUrl());

            //Then
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthRequestToken(any());
            verifyNoMoreInteractions(userInfoJpaRepository, twitterFactory);
            verifyZeroInteractions(twitterUserJpaRepository, requestTokenStorage, driveService);
        }
    }

    @Nested
    class CreateTwitterUser {
        UserInfo userInfo;
        String oauthToken;
        String oauthVerifier;
        RequestToken requestToken;
        AccessToken accessToken;
        User user;

        @BeforeEach
        void setUp() {
            userInfo = new UserInfoBuilder().id(7L).login("userLogin").password("password").role(ADMIN).build();
            oauthToken = "oauth-token";
            oauthVerifier = "oauth-verifier";
            requestToken = new RequestToken("token", "token-secret");
            accessToken = new AccessToken("access-token", "access-token-secret");
            user = mock(User.class);
            when(user.getId()).thenReturn(1234567L);
            when(user.getScreenName()).thenReturn("TwitterUserScreenName");
        }

        @Test
        void success() throws TwitterException {
            //Given
            TwitterUser expected = TwitterUserBuilder.getInstance()
                    .name("TwitterUserScreenName").socialId(1234567L).userInfo(userInfo).token("access-token").tokenSecret("access-token-secret")
                    .build();
            when(requestTokenStorage.retrieveAndRemoveRequestToken(eq(oauthToken))).thenReturn(Optional.of(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, oauthVerifier)).thenReturn(accessToken);
            when(twitter.verifyCredentials()).thenReturn(user);
            when(twitterUserJpaRepository.findBySocialId(expected.getSocialId())).thenReturn(Optional.empty());

            //When
            TwitterUser twitterUser = instance.createTwitterUser(userInfo, oauthToken, oauthVerifier);

            //Then
            assertThat(twitterUser).isEqualToIgnoringGivenFields(expected, "id");
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(any(String.class));
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthAccessToken(any(RequestToken.class), any(String.class));
            verify(twitter, times(1)).verifyCredentials();
            verify(twitterUserJpaRepository, times(1)).findBySocialId(expected.getSocialId());
            verify(twitterUserJpaRepository, times(1)).saveAndFlush(twitterUser);
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }

        @Test
        void tokenNotFoundInStorage() {
            //Given
            when(requestTokenStorage.retrieveAndRemoveRequestToken(any())).thenReturn(Optional.empty());

            //When
            assertThrows(AssociateTwitterAccountException.class, () -> instance.createTwitterUser(userInfo, oauthToken, oauthVerifier));

            //Then
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(any(String.class));
            verifyNoMoreInteractions(requestTokenStorage);
            verifyZeroInteractions(twitterUserJpaRepository, userInfoJpaRepository, driveService, twitterFactory);
        }

        @Test
        void getOAuthAccessTokenWithException() throws TwitterException {
            //Given
            when(requestTokenStorage.retrieveAndRemoveRequestToken(eq(oauthToken))).thenReturn(Optional.of(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, oauthVerifier)).thenThrow(TwitterException.class);

            //When
            assertThrows(AssociateTwitterAccountException.class, () -> instance.createTwitterUser(userInfo, oauthToken, oauthVerifier));

            //Then
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(any(String.class));
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthAccessToken(any(RequestToken.class), any(String.class));
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory);
            verifyZeroInteractions(twitterUserJpaRepository, userInfoJpaRepository, driveService);
        }

        @Test
        void userWithSocialIdExistsInTwitterUserJpaRepository() throws TwitterException {
            //Given
            TwitterUser expected = TwitterUserBuilder.getInstance()
                    .name("TwitterUserScreenName").socialId(1234567L).userInfo(userInfo).token("access-token").tokenSecret("access-token-secret")
                    .build();
            when(requestTokenStorage.retrieveAndRemoveRequestToken(eq(oauthToken))).thenReturn(Optional.of(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, oauthVerifier)).thenReturn(accessToken);
            when(twitter.verifyCredentials()).thenReturn(user);
            when(twitterUserJpaRepository.findBySocialId(expected.getSocialId())).thenReturn(Optional.of(new TwitterUser()));

            //When
            assertThrows(TwitterAccountAlreadyAssociateException.class, () -> instance.createTwitterUser(userInfo, oauthToken, oauthVerifier));

            //Then
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(any(String.class));
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(twitter, times(1)).getOAuthAccessToken(any(RequestToken.class), any(String.class));
            verify(twitter, times(1)).verifyCredentials();
            verify(twitterUserJpaRepository, times(1)).findBySocialId(expected.getSocialId());
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }
    }

    @Nested
    class AssociateAccount {
        private final static String LOGIN = "userLogin";

        UserInfo userInfo;
        String oauthToken;
        String oauthVerifier;
        TwitterUser twitterUser;

        @BeforeEach
        void setUp() {
            userInfo = new UserInfoBuilder().id(7L).login(LOGIN).password("password").role(ADMIN).build();
            oauthToken = "oauth-token";
            oauthVerifier = "oauth-verifier";
            twitterUser = TwitterUserBuilder.getInstance()
                    .name("screenName").socialId(1234567L).token("token").tokenSecret("token-secret").userInfo(userInfo)
                    .build();
        }

        @Test
        void success() {
            //Given
            TwitterUserDTO expected = TwitterUserDTOBuilder.getInstance()
                    .name("screenName").socialId(1234567L)
                    .build();
            TwitterServiceImpl spyInstance = spy(instance);

            when(userInfoJpaRepository.findByLogin(LOGIN)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.empty());
            doReturn(twitterUser).when(spyInstance).createTwitterUser(userInfo, oauthToken, oauthVerifier);

            //When
            TwitterUserDTO twitterUserDTO = spyInstance.associateAccount(LOGIN, oauthToken, oauthVerifier);

            //Then
            assertThat(twitterUserDTO)
                    .isEqualToIgnoringGivenFields(expected, "id");
            verify(userInfoJpaRepository, times(1)).findByLogin(any(String.class));
            verify(twitterUserJpaRepository, times(1)).findByUserInfo(any(UserInfo.class));
            verify(twitterUserJpaRepository, times(1)).saveAndFlush(any(TwitterUser.class));
        }

        @Test
        void userInfoNotFound() {
            //Given
            when(userInfoJpaRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

            //When
            assertThrows(AssociateTwitterAccountException.class, () -> instance.associateAccount(LOGIN, oauthToken, oauthVerifier));
        }

        @Test
        void twitterUserInfoExists() {
            //Given
            when(userInfoJpaRepository.findByLogin(LOGIN)).thenReturn(Optional.of(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.of(new TwitterUser()));

            //When
            assertThrows(TwitterAccountAlreadyAssociateException.class, () -> instance.associateAccount(LOGIN, oauthToken, oauthVerifier));
        }
    }

    @Nested
    class FindUserInfoByRequestToken {
        String token;
        String verifier;
        RequestToken requestToken;
        AccessToken accessToken;
        User user;
        TwitterUser twitterUser;
        UserInfo userInfo;

        @BeforeEach
        void setUp() {
            token = "some-token";
            verifier = "token-verifier";
            requestToken = new RequestToken("token", "token-secret");
            accessToken = new AccessToken("access-token", "access-token-secret");
            user = mock(User.class);
            when(user.getId()).thenReturn(1234567L);
            when(user.getScreenName()).thenReturn("TwitterUserScreenName");
            userInfo = new UserInfoBuilder().id(7L).login("userLogin").password("password").role(ADMIN).build();
            twitterUser = TwitterUserBuilder.getInstance()
                    .name("TwitterUserScreenName").socialId(1234567L).token("old-token").tokenSecret("old-token-secret").userInfo(userInfo)
                    .build();

            when(twitterUserJpaRepository.saveAndFlush(any(TwitterUser.class)))
                    .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        }

        @Test
        void success() throws TwitterException {
            //Given
            UserInfoDTO expected = toDto(userInfo);
            when(requestTokenStorage.retrieveAndRemoveRequestToken(token)).thenReturn(Optional.ofNullable(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, verifier)).thenReturn(accessToken);
            when(twitter.verifyCredentials()).thenReturn(user);
            when(twitterUserJpaRepository.findBySocialId(user.getId())).thenReturn(Optional.ofNullable(twitterUser));

            //When
            UserInfoDTO userInfoDTO = instance.findUserInfoByRequestToken(token, verifier);

            //Then
            assertThat(userInfoDTO).isEqualToComparingFieldByField(expected);
            assertThat(twitterUser.getToken()).isEqualTo(accessToken.getToken());
            assertThat(twitterUser.getTokenSecret()).isEqualTo(accessToken.getTokenSecret());
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(token);
            verify(twitter, times(1)).getOAuthAccessToken(requestToken, verifier);
            verify(twitter, times(1)).verifyCredentials();
            verify(twitterUserJpaRepository, times(1)).findBySocialId(user.getId());
            verify(twitterUserJpaRepository, times(1)).saveAndFlush(any(TwitterUser.class));
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }

        @Test
        void notFoundTwitterUserInRepository() throws TwitterException {
            //Given
            when(requestTokenStorage.retrieveAndRemoveRequestToken(token)).thenReturn(Optional.ofNullable(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, verifier)).thenReturn(accessToken);
            when(twitter.verifyCredentials()).thenReturn(user);
            when(twitterUserJpaRepository.findBySocialId(user.getId())).thenReturn(Optional.empty());

            //When
            assertThrows(TwitterAuthenticationException.class, () -> instance.findUserInfoByRequestToken(token, verifier));

            //Then
            assertThat(twitterUser.getToken()).isEqualTo("old-token");
            assertThat(twitterUser.getTokenSecret()).isEqualTo("old-token-secret");
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(token);
            verify(twitter, times(1)).getOAuthAccessToken(requestToken, verifier);
            verify(twitter, times(1)).verifyCredentials();
            verify(twitterUserJpaRepository, times(1)).findBySocialId(user.getId());
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }

        @Test
        void verifyCredentialsFailure() throws TwitterException {
            //Given
            when(requestTokenStorage.retrieveAndRemoveRequestToken(token)).thenReturn(Optional.ofNullable(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, verifier)).thenReturn(accessToken);
            when(twitter.verifyCredentials()).thenThrow(TwitterException.class);

            //When
            assertThrows(TwitterAuthenticationException.class, () -> instance.findUserInfoByRequestToken(token, verifier));

            //Then
            assertThat(twitterUser.getToken()).isEqualTo("old-token");
            assertThat(twitterUser.getTokenSecret()).isEqualTo("old-token-secret");
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(token);
            verify(twitter, times(1)).getOAuthAccessToken(requestToken, verifier);
            verify(twitter, times(1)).verifyCredentials();
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }

        @Test
        void getOAuthAccessTokenFailure() throws TwitterException {
            //Given
            when(requestTokenStorage.retrieveAndRemoveRequestToken(token)).thenReturn(Optional.ofNullable(requestToken));
            when(twitter.getOAuthAccessToken(requestToken, verifier)).thenThrow(TwitterException.class);

            //When
            assertThrows(TwitterAuthenticationException.class, () -> instance.findUserInfoByRequestToken(token, verifier));

            //Then
            assertThat(twitterUser.getToken()).isEqualTo("old-token");
            assertThat(twitterUser.getTokenSecret()).isEqualTo("old-token-secret");
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(token);
            verify(twitter, times(1)).getOAuthAccessToken(requestToken, verifier);
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }

        @Test
        void requestTokenNotFound() {
            //Given
            when(requestTokenStorage.retrieveAndRemoveRequestToken(token)).thenReturn(Optional.empty());

            //When
            assertThrows(TwitterAuthenticationException.class, () -> instance.findUserInfoByRequestToken(token, verifier));

            //Then
            assertThat(twitterUser.getToken()).isEqualTo("old-token");
            assertThat(twitterUser.getTokenSecret()).isEqualTo("old-token-secret");
            verify(twitterFactory, times(1)).getTwitterInstance();
            verify(requestTokenStorage, times(1)).retrieveAndRemoveRequestToken(token);
            verifyNoMoreInteractions(requestTokenStorage, twitter, twitterFactory, twitterUserJpaRepository);
            verifyZeroInteractions(userInfoJpaRepository, driveService);
        }
    }

    @Nested
    class FindAccountByLogin {
        String login;
        UserInfo userInfo;
        TwitterUser twitterUser;

        @BeforeEach
        void setUp() {
            login = "userLogin";
            userInfo = new UserInfoBuilder().id(7L).login(login).password("password").role(ADMIN).build();
            twitterUser = TwitterUserBuilder.getInstance()
                    .name("TwitterUserScreenName").socialId(1234567L).token("old-token").tokenSecret("old-token-secret").userInfo(userInfo)
                    .build();

        }

        @Test
        void success() {
            //Given
            TwitterUserDTO expected = new TwitterUserDTO();
            expected.setId(twitterUser.getId());
            expected.setSocialId(twitterUser.getSocialId());
            expected.setName(twitterUser.getName());
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.ofNullable(twitterUser));

            //When
            TwitterUserDTO twitterUserDTO = instance.findAccountByLogin(login);

            //Then
            assertThat(twitterUserDTO).isEqualToComparingFieldByField(expected);
            verify(userInfoJpaRepository, times(1)).findByLogin(login);
            verify(twitterUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(userInfoJpaRepository, twitterUserJpaRepository);
            verifyZeroInteractions(requestTokenStorage, twitter, twitterFactory, userInfoJpaRepository, driveService);
        }

        @Test
        void twitterUserNotFound() {
            //Given
            TwitterUserDTO expected = new TwitterUserDTO();
            expected.setId(twitterUser.getId());
            expected.setSocialId(twitterUser.getSocialId());
            expected.setName(twitterUser.getName());
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.empty());

            //When
            TwitterUserDTO twitterUserDTO = instance.findAccountByLogin(login);

            //Then
            assertThat(twitterUserDTO).isNull();
            verify(userInfoJpaRepository, times(1)).findByLogin(login);
            verify(twitterUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(userInfoJpaRepository, twitterUserJpaRepository);
            verifyZeroInteractions(requestTokenStorage, twitter, twitterFactory, userInfoJpaRepository, driveService);
        }

        @Test
        void userInfoNotFound() {
            //Given
            TwitterUserDTO expected = new TwitterUserDTO();
            expected.setId(twitterUser.getId());
            expected.setSocialId(twitterUser.getSocialId());
            expected.setName(twitterUser.getName());
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.empty());

            //When
            TwitterUserDTO twitterUserDTO = instance.findAccountByLogin(login);

            //Then
            assertThat(twitterUserDTO).isNull();
            verify(userInfoJpaRepository, times(1)).findByLogin(login);
            verifyNoMoreInteractions(userInfoJpaRepository, twitterUserJpaRepository);
            verifyZeroInteractions(requestTokenStorage, twitter, twitterFactory, userInfoJpaRepository, driveService);
        }
    }

    @Nested
    class DisassociateAccount {
        String login;
        UserInfo userInfo;
        TwitterUser twitterUser;

        @BeforeEach
        void setUp() {
            login = "userLogin";
            userInfo = new UserInfoBuilder().id(7L).login(login).password("password").role(ADMIN).build();
            twitterUser = TwitterUserBuilder.getInstance()
                    .name("TwitterUserScreenName").socialId(1234567L).token("old-token").tokenSecret("old-token-secret").userInfo(userInfo)
                    .build();

        }

        @Test
        void success() {
            //Given
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.ofNullable(twitterUser));

            //When
            instance.disassociateAccount(login);

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin(login);
            verify(twitterUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verify(twitterUserJpaRepository, times(1)).deleteByUserInfo(userInfo);
            verifyNoMoreInteractions(userInfoJpaRepository, twitterUserJpaRepository);
            verifyZeroInteractions(requestTokenStorage, twitter, twitterFactory, userInfoJpaRepository, driveService);
        }

        @Test
        void twitterUserNotFound() {
            //Given
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.empty());

            //When
            assertThrows(DisassociateTwitterAccountException.class, () -> instance.disassociateAccount(login));

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin(login);
            verify(twitterUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(userInfoJpaRepository, twitterUserJpaRepository);
            verifyZeroInteractions(requestTokenStorage, twitter, twitterFactory, userInfoJpaRepository, driveService);
        }

        @Test
        void userInfoNotFound() {
            //Given
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.empty());

            //When
            assertThrows(DisassociateTwitterAccountException.class, () -> instance.disassociateAccount(login));

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin(login);
            verifyNoMoreInteractions(userInfoJpaRepository, twitterUserJpaRepository);
            verifyZeroInteractions(requestTokenStorage, twitter, twitterFactory, userInfoJpaRepository, driveService);
        }
    }

    @Nested
    class RetrieveImagesData {
        String login;
        String[] paths;
        TweetDTO tweetDTO;

        @BeforeEach
        void setUp() {
            login = "awesomeUser";
            paths = new String[]{"user/awesomeUser/drive/picture.jpg", "user/awesomeUser/drive/image.jpg"};
            tweetDTO = new TweetDTO();
            tweetDTO.setMessage("Tweet message");
            tweetDTO.setPaths(asList(paths[0], paths[1]));
        }

        @Test
        void success() {
            //Given
            when(driveService.retrievePictureFileData(eq(login), any(String.class)))
                    .thenAnswer(invocationOnMock -> ((String) invocationOnMock.getArguments()[1]).getBytes());
            //When
            List<byte[]> imagesData = instance.retrieveImagesData(login, tweetDTO);

            //Then
            assertThat(imagesData).hasSize(2)
                    .containsExactlyInAnyOrder(paths[0].getBytes(), paths[1].getBytes());
        }

        @Test
        void whenDriveExceptionWasOccurred() {
            //Given
            when(driveService.retrievePictureFileData(eq(login), any(String.class))).thenThrow(DriveException.class);

            //When
            assertThrows(TweetPublishingException.class, () -> instance.retrieveImagesData(login, tweetDTO));
        }

        @Test
        void tweetDtoHasNoImages() {
            //Given
            tweetDTO.setPaths(emptyList());

            //When
            List<byte[]> imagesData = instance.retrieveImagesData(login, tweetDTO);

            //Then
            assertThat(imagesData).isEmpty();
            verifyZeroInteractions(driveService);
        }
    }

    @Test
    void prepareImages() throws TwitterException {
        //Given
        List<byte[]> images = asList("first".getBytes(), "second".getBytes());

        when(twitter.uploadMedia(any(String.class), any(InputStream.class)))
                .thenAnswer(invocationOnMock -> {
                    UploadedMedia mock = mock(UploadedMedia.class);
                    when(mock.getMediaId()).thenReturn(123L);

                    return mock;
                });

        //When
        long[] ids = instance.prepareImages(twitter, images);

        //Then
        assertThat(ids)
                .hasSize(2)
                .containsAnyOf(123L);
        verify(twitter, times(2)).uploadMedia(any(String.class), any(InputStream.class));
    }

    @Nested
    class PublishTweet {
        String login;
        UserInfo userInfo;
        TwitterUser twitterUser;
        List<byte[]> images;
        User user;
        Status status;
        TweetDTO tweetDTO;

        @BeforeEach
        void setUp() {
            login = "userLogin";
            userInfo = new UserInfoBuilder().id(7L).login(login).password("password").role(ADMIN).build();
            twitterUser = TwitterUserBuilder.getInstance()
                    .name("TwitterUserScreenName").socialId(1234567L).token("old-token").tokenSecret("old-token-secret").userInfo(userInfo)
                    .build();
            images = singletonList("image.jpg".getBytes());
            user = mock(User.class);
            when(user.getScreenName()).thenReturn("userScreenName");
            when(user.getId()).thenReturn(777L);
            status = mock(Status.class);
            when(status.getUser()).thenReturn(user);
            when(status.getId()).thenReturn(7654321L);
            tweetDTO = new TweetDTO();
            tweetDTO.setMessage("Tweet message");
            tweetDTO.setPaths(singletonList("mockPath"));
        }

        @Test
        void success() throws TwitterException {
            //Given
            TwitterServiceImpl spyInstance = spy(instance);
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.ofNullable(twitterUser));
            doReturn(images).when(spyInstance).retrieveImagesData(eq(login), any(TweetDTO.class));
            when(twitterFactory.getTwitterInstance(twitterUser)).thenReturn(twitter);
            doReturn(new long[]{234L}).when(spyInstance).prepareImages(twitter, images);
            when(twitter.updateStatus(any(StatusUpdate.class))).thenReturn(status);

            //When
            TweetDTO tweetDTO = spyInstance.publishTweet(login, this.tweetDTO);

            //Then
            assertThat(tweetDTO.getUrl()).isEqualTo("https://twitter.com/userScreenName/status/7654321");
        }


        @Test
        void updateStatusFailure() throws TwitterException {
            //Given
            TwitterServiceImpl spyInstance = spy(instance);
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.ofNullable(twitterUser));
            doReturn(images).when(spyInstance).retrieveImagesData(eq(login), any(TweetDTO.class));
            when(twitterFactory.getTwitterInstance(twitterUser)).thenReturn(twitter);
            doReturn(new long[]{234L}).when(spyInstance).prepareImages(twitter, images);
            when(twitter.updateStatus(any(StatusUpdate.class))).thenThrow(TwitterException.class);

            //When
            assertThrows(TweetPublishingException.class, () -> spyInstance.publishTweet(login, this.tweetDTO));
        }

        @Test
        void nullPointerExceptionWasOccurred() throws TwitterException {
            //Given
            TwitterServiceImpl spyInstance = spy(instance);
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.ofNullable(twitterUser));
            doReturn(images).when(spyInstance).retrieveImagesData(eq(login), any(TweetDTO.class));
            when(twitterFactory.getTwitterInstance(twitterUser)).thenReturn(twitter);
            doReturn(new long[]{234L}).when(spyInstance).prepareImages(twitter, images);
            when(twitter.updateStatus(any(StatusUpdate.class))).thenReturn(null);


            //When
            assertThrows(TweetPublishingException.class, () -> spyInstance.publishTweet(login, this.tweetDTO));
        }

        @Test
        void prepareImageWithException() throws TwitterException {
            //Given
            TwitterServiceImpl spyInstance = spy(instance);
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.ofNullable(twitterUser));
            doReturn(images).when(spyInstance).retrieveImagesData(eq(login), any(TweetDTO.class));
            when(twitterFactory.getTwitterInstance(twitterUser)).thenReturn(twitter);
            doThrow(TwitterException.class).when(spyInstance).prepareImages(twitter, images);

            //When
            assertThrows(TweetPublishingException.class, () -> spyInstance.publishTweet(login, this.tweetDTO));
        }

        @Test
        void twitterUserNotFound() {
            //Given
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.ofNullable(userInfo));
            when(twitterUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.empty());

            //When
            assertThrows(NotFoundTwitterAssociatedAccountException.class, () -> instance.publishTweet(login, this.tweetDTO));
        }

        @Test
        void userInfoNotFound() {
            //Given
            when(userInfoJpaRepository.findByLogin(login)).thenReturn(Optional.empty());

            //When
            assertThrows(NotFoundTwitterAssociatedAccountException.class, () -> instance.publishTweet(login, this.tweetDTO));
        }
    }
}