package ua.kiev.prog.photopond.facebook;


import com.restfb.FacebookClient;
import com.restfb.exception.FacebookQueryParseException;
import com.restfb.types.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.facebook.exception.AssociateFBAccountException;
import ua.kiev.prog.photopond.facebook.exception.DisassociateFBAccountException;
import ua.kiev.prog.photopond.facebook.exception.FBAccountAlreadyAssociateException;
import ua.kiev.prog.photopond.facebook.exception.FBAuthenticationException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.user.UserInfoMapper.toDto;

@ExtendWith(SpringExtension.class)
class FBServiceImplTest {
    private FBServiceImpl instance;

    @MockBean
    private FBUserJpaRepository fbUserJpaRepository;

    @MockBean
    private UserInfoJpaRepository userInfoJpaRepository;

    @BeforeEach
    void setUp() {
        Mockito.reset(fbUserJpaRepository, userInfoJpaRepository);
        instance = new FBServiceImpl(fbUserJpaRepository, userInfoJpaRepository);
    }

    @Test
    void changeCodeToExtendedAccessToken() {
        //Given
        final FacebookClient.AccessToken userAccessToken = Mockito.mock(FacebookClient.AccessToken.class);
        final FacebookClient.AccessToken extendedAccessToken = Mockito.mock(FacebookClient.AccessToken.class);
        FacebookClient facebookClient = Mockito.mock(FacebookClient.class);

        when(facebookClient.obtainUserAccessToken(or(isNull(), any()), or(isNull(), any()), or(isNull(), any()), any(String.class)))
                .thenReturn(userAccessToken);
        when(userAccessToken.getAccessToken()).thenReturn("token");
        when(facebookClient.obtainExtendedAccessToken(or(isNull(), any()), or(isNull(), any()), eq("token"))).thenReturn(extendedAccessToken);

        //When
        FacebookClient.AccessToken token = instance.changeCodeToExtendedAccessToken("code", facebookClient);

        //Then
        assertThat(token)
                .isEqualTo(extendedAccessToken);
        verify(facebookClient).obtainUserAccessToken(or(isNull(), any()), or(isNull(), any()), or(isNull(), any()), any(String.class));
        verify(facebookClient).obtainExtendedAccessToken(or(isNull(), any()), or(isNull(), any()), any(String.class));
        verifyNoMoreInteractions(facebookClient);
        verify(userAccessToken).getAccessToken();
        verifyNoMoreInteractions(userAccessToken);
        verifyNoMoreInteractions(extendedAccessToken);
    }

    @Test
    void retrieveUserByFacebookClient() {
        //Given
        final User expectedUser = new User();
        FacebookClient facebookClient = mock(FacebookClient.class);
        when(facebookClient.fetchObject(any(), any(), any())).thenReturn(expectedUser);

        //When
        Optional<User> result = instance.retrieveUser(facebookClient);

        //Then
        assertThat(result)
                .isPresent()
                .get()
                .isEqualTo(expectedUser);
    }

    @Test
    void retrieveUserByFacebookClientIsNull() {
        //When
        Optional<User> result = instance.retrieveUser((FacebookClient) null);

        //Then
        assertThat(result)
                .isNotPresent();
    }

    @Test
    void retrieveUserByAccessToken() {
        //Given
        final User expectedUser = new User();
        final FacebookClient.AccessToken accessToken = mock(FacebookClient.AccessToken.class);
        FBServiceImpl fbService = spy(instance);
        doReturn(Optional.of(expectedUser)).when(fbService).retrieveUser(any(FacebookClient.class));
        FacebookClient facebookClient = mock(FacebookClient.class);
        when(facebookClient.fetchObject(any(), any(), any())).thenReturn(expectedUser);
        when(accessToken.getAccessToken()).thenReturn("token");


        //When
        Optional<User> result = fbService.retrieveUser(accessToken);

        //Then
        assertThat(result)
                .isPresent()
                .get()
                .isEqualTo(expectedUser);
    }

    @Test
    void retrieveUserByAccessTokenIsNull() {
        //When
        Optional<User> result = instance.retrieveUser((FacebookClient.AccessToken) null);

        //Then
        assertThat(result)
                .isNotPresent();
    }

    @Nested
    class AssociateAccount {
        @Test
        void notPresentInUserInfoJpaRepository() {
            //Given
            when(userInfoJpaRepository.findByLogin(any())).thenReturn(Optional.empty());

            //When
            assertThrows(AssociateFBAccountException.class, () -> instance.associateAccount("login", "code"));

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void presentInFbUserJpaRepository() {
            //Given
            final UserInfo userInfo = new UserInfo("login", "password");
            when(userInfoJpaRepository.findByLogin(eq("login"))).thenReturn(Optional.of(userInfo));
            when(fbUserJpaRepository.findByUserInfo(userInfo)).thenReturn(Optional.of(new FBUser()));

            //When
            assertThrows(FBAccountAlreadyAssociateException.class, () -> instance.associateAccount("login", "code"));

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verify(fbUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void success() {
            //Given
            final UserInfo userInfo = new UserInfo("login", "password");
            final FBUser fbUser = FBUserBuilder.getInstance()
                    .id(123L).name("fbUserName").email("some@email.com").fbId("1234567").userInfo(userInfo)
                    .build();
            FBUserDTO expected = FBUserMapper.toDto(fbUser);
            when(userInfoJpaRepository.findByLogin(eq("login"))).thenReturn(Optional.of(userInfo));
            when(fbUserJpaRepository.findByUserInfo(eq(userInfo))).thenReturn(Optional.empty());
            FBServiceImpl fbService = spy(instance);
            doReturn(fbUser).when(fbService).createFBUser(userInfo, "code");

            //When
            FBUserDTO fbUserDTO = fbService.associateAccount("login", "code");

            //Then
            assertThat(fbUserDTO)
                    .isNotNull()
                    .isEqualToComparingFieldByFieldRecursively(expected);
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verify(fbUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verify(fbUserJpaRepository, times(1)).saveAndFlush(fbUser);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }
    }

    @Nested
    class CreateFBUser {
        FBServiceImpl fbService;

        @BeforeEach
        void setUp() {
            fbService = spy(instance);
        }

        @Test
        void facebookClientError() {
            //Given
            doThrow(FacebookQueryParseException.class).when(fbService).changeCodeToExtendedAccessToken(any());

            //When
            assertThrows(AssociateFBAccountException.class, () -> fbService.createFBUser(new UserInfo("login", "password"), "code"));

            //Then
            verify(fbService).changeCodeToExtendedAccessToken("code");
        }

        @Test
        void retrieveUserError() {
            //Given
            final FacebookClient.AccessToken accessToken = new FacebookClient.AccessToken();
            doReturn(accessToken).when(fbService).changeCodeToExtendedAccessToken(any());
            doReturn(Optional.empty()).when(fbService).retrieveUser(accessToken);

            //When
            assertThrows(AssociateFBAccountException.class, () -> fbService.createFBUser(new UserInfo("login", "password"), "code"));

            //Then
            verify(fbService).changeCodeToExtendedAccessToken("code");
            verify(fbService).retrieveUser(accessToken);
        }


        @DisplayName(value = "FbId already exists in database")
        @ParameterizedTest(name = "[{index}] ==> count = {0}")
        @ValueSource(longs = {1, 2, 4})
        void accountAlreadyAssociate(long count) {
            //Given
            final FacebookClient.AccessToken accessToken = new FacebookClient.AccessToken();
            final User user = new User();
            user.setId("1234567");
            doReturn(accessToken).when(fbService).changeCodeToExtendedAccessToken(any());
            doReturn(Optional.of(user)).when(fbService).retrieveUser(accessToken);
            when(fbUserJpaRepository.countByFbId("1234567")).thenReturn(count);

            //When
            assertThrows(AssociateFBAccountException.class, () -> fbService.createFBUser(new UserInfo("login", "password"), "code"));

            //Then
            verify(fbService).changeCodeToExtendedAccessToken("code");
            verify(fbService).retrieveUser(accessToken);
            verify(fbUserJpaRepository, times(1)).countByFbId("1234567");
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void success() {
            //Given
            final FacebookClient.AccessToken accessToken = mock(FacebookClient.AccessToken.class);
            final UserInfo userInfo = new UserInfo("login", "password");
            final User user = new User();
            final Date expires = new Date();
            LocalDateTime expectedExpires = expires.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String fbId = "1234567";
            String email = "some@email.com";
            String name = "userName";
            user.setId(fbId);
            user.setEmail(email);
            user.setName(name);
            doReturn(accessToken).when(fbService).changeCodeToExtendedAccessToken(any());
            doReturn(Optional.of(user)).when(fbService).retrieveUser(accessToken);
            when(accessToken.getAccessToken()).thenReturn("accessToken");
            when(accessToken.getExpires()).thenReturn(expires);
            when(fbUserJpaRepository.countByFbId(fbId)).thenReturn(0L);

            FBUser expectedFbUser = FBUserBuilder.getInstance()
                    .fbId(fbId).email(email).name(name).userInfo(userInfo).accessToken("accessToken").tokenExpires(expectedExpires)
                    .build();

            //When
            FBUser fbUser = fbService.createFBUser(userInfo, "code");

            //Then
            assertThat(fbUser).isEqualToIgnoringGivenFields(expectedFbUser, "id");
            verify(fbService).changeCodeToExtendedAccessToken("code");
            verify(fbService).retrieveUser(accessToken);
            verify(fbUserJpaRepository, times(1)).countByFbId(fbId);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }
    }

    @Nested
    class FindUserInfoByCode {
        FBServiceImpl fbService;
        FacebookClient.AccessToken accessToken;
        User user;
        UserInfo userInfo;
        Date expires;
        LocalDateTime expectedExpires;
        FBUser fbUser;
        FBUser expectedFbUser;

        @BeforeEach
        void setUp() {
            fbService = spy(instance);
            accessToken = mock(FacebookClient.AccessToken.class);

            userInfo = new UserInfo("login", "password");
            user = new User();
            expires = new Date();
            expectedExpires = expires.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String fbId = "1234567";
            String email = "some@email.com";
            String name = "userName";
            user.setId(fbId);
            user.setEmail(email);
            user.setName(name);

            fbUser = FBUserBuilder.getInstance()
                    .fbId(fbId).email(email).name(name).userInfo(userInfo)
                    .accessToken("oldAccessToken").tokenExpires(expectedExpires.minus(10, ChronoUnit.DAYS))
                    .build();
            expectedFbUser = FBUserBuilder.getInstance()
                    .fbId(fbId).email(email).name(name).userInfo(userInfo)
                    .accessToken("accessToken").tokenExpires(expectedExpires)
                    .build();

            doReturn(accessToken).when(fbService).changeCodeToExtendedAccessToken(any());
            doReturn(Optional.of(user)).when(fbService).retrieveUser(accessToken);
            when(accessToken.getAccessToken()).thenReturn("accessToken");
            when(accessToken.getExpires()).thenReturn(expires);
        }

        @Test
        void userNotExistsInDatabase() {
            //Given
            when(fbUserJpaRepository.findByFbId(user.getId())).thenReturn(Optional.empty());

            //When
            assertThrows(FBAuthenticationException.class, () -> fbService.findUserInfoByCode("code"));

            //Then
            verify(fbUserJpaRepository, times(1)).findByFbId(expectedFbUser.getFbId());
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void success() {
            //Given
            when(fbUserJpaRepository.findByFbId(user.getId())).thenReturn(Optional.of(fbUser));
            when(fbUserJpaRepository.saveAndFlush(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

            //When
            UserInfoDTO userInfoDTO = fbService.findUserInfoByCode("code");

            //Then
            verify(fbUserJpaRepository, times(1)).findByFbId(expectedFbUser.getFbId());
            verify(fbUserJpaRepository, times(1)).saveAndFlush(any());
            verifyNoMoreInteractions(fbUserJpaRepository);
            assertThat(fbUser).isEqualToIgnoringGivenFields(expectedFbUser, "id");
            assertThat(userInfoDTO).isEqualToComparingFieldByFieldRecursively(toDto(expectedFbUser.getUserInfo()));
        }
    }

    @Nested
    class FindAccountByLogin {
        @Test
        void notPresentInUserInfoJpaRepository() {
            //Given
            when(userInfoJpaRepository.findByLogin(any())).thenReturn(Optional.empty());

            //When
            FBUserDTO fbUserDTO = instance.findAccountByLogin("login");

            //Then
            assertThat(fbUserDTO).isNull();
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void notPresentInFbUserJpaRepository() {
            //Given
            final UserInfo userInfo = new UserInfo("login", "password");
            when(userInfoJpaRepository.findByLogin(eq("login"))).thenReturn(Optional.of(userInfo));
            when(fbUserJpaRepository.findByUserInfo(any())).thenReturn(Optional.empty());

            //When
            FBUserDTO fbUserDTO = instance.findAccountByLogin("login");

            //Then
            assertThat(fbUserDTO).isNull();
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verify(fbUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void success() {
            //Given
            final UserInfo userInfo = new UserInfo("login", "password");
            final FBUser fbUser = FBUserBuilder.getInstance()
                    .id(123L).name("fbUserName").email("some@email.com").fbId("1234567").userInfo(userInfo)
                    .build();
            FBUserDTO expected = FBUserMapper.toDto(fbUser);
            when(userInfoJpaRepository.findByLogin(eq("login"))).thenReturn(Optional.of(userInfo));
            when(fbUserJpaRepository.findByUserInfo(eq(userInfo))).thenReturn(Optional.of(fbUser));

            //When
            FBUserDTO fbUserDTO = instance.findAccountByLogin("login");

            //Then
            assertThat(fbUserDTO)
                    .isNotNull()
                    .isEqualToComparingFieldByFieldRecursively(expected);
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verify(fbUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }
    }

    @Nested
    class DisassociateAccount {
        @Test
        void notPresentInUserInfoJpaRepository() {
            //Given
            when(userInfoJpaRepository.findByLogin(any())).thenReturn(Optional.empty());

            //When
            assertThrows(DisassociateFBAccountException.class, () -> instance.disassociateAccount("login"));

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void notPresentInFbUserJpaRepository() {
            //Given
            final UserInfo userInfo = new UserInfo("login", "password");
            when(userInfoJpaRepository.findByLogin(eq("login"))).thenReturn(Optional.of(userInfo));
            when(fbUserJpaRepository.findByUserInfo(any())).thenReturn(Optional.empty());

            //When
            assertThrows(DisassociateFBAccountException.class, () -> instance.disassociateAccount("login"));

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verify(fbUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }

        @Test
        void success() {
            //Given
            FBServiceImpl fbService = spy(instance);
            final UserInfo userInfo = new UserInfo("login", "password");
            final FBUser fbUser = FBUserBuilder.getInstance()
                    .id(123L).name("fbUserName").email("some@email.com").fbId("1234567").userInfo(userInfo)
                    .build();
            when(userInfoJpaRepository.findByLogin(eq("login"))).thenReturn(Optional.of(userInfo));
            when(fbUserJpaRepository.findByUserInfo(eq(userInfo))).thenReturn(Optional.of(fbUser));
            doNothing().when(fbService).removeFacebookPermissions(any());

            //When
            fbService.disassociateAccount("login");

            //Then
            verify(userInfoJpaRepository, times(1)).findByLogin("login");
            verifyNoMoreInteractions(userInfoJpaRepository);
            verify(fbUserJpaRepository, times(1)).findByUserInfo(userInfo);
            verify(fbUserJpaRepository, times(1)).deleteByUserInfo(userInfo);
            verifyNoMoreInteractions(fbUserJpaRepository);
        }
    }
}