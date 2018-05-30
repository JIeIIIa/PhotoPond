package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoServiceJpaImplTest {

    @Mock
    private UserInfoJpaRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private UserInfoServiceJpaImpl instance;

    private final String USER_LOGIN = "user";

    private UserInfo mockUser;

    public UserInfoServiceJpaImplTest() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Before
    public void setUp() {
        instance = new UserInfoServiceJpaImpl(userRepository, passwordEncoder);
        mockUser = new UserInfoBuilder().id(777).login("mockUser").password("password").role(UserRole.USER).build();
    }

    @Test
    public void successExistUserByLogin() {
        when(userRepository.findByLogin(mockUser.getLogin()))
                .thenReturn(Optional.of(mockUser));

        assertThat(instance.existByLogin(mockUser.getLogin()))
                .isTrue();
        verify(userRepository).findByLogin(mockUser.getLogin());
    }

    @Test
    public void failureExistUserByLogin() {
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        assertThat(instance.existByLogin(USER_LOGIN))
                .isFalse();
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    public void successAddUserTest() {
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);

        instance.addUser(user);

        verify(userRepository).save(eq(user));
    }

    @Test
    public void AddNullAsUserTest() {
        instance.addUser(null);

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    public void findExistUserByLogin() {
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        assertThat(instance.getUserByLogin(USER_LOGIN))
                .isNotNull()
                .isPresent()
                .hasValue(user);
        verify(userRepository).findByLogin(USER_LOGIN);
    }


    @Test
    public void findNotExistUserByLogin() {
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        assertThat(instance.getUserByLogin(USER_LOGIN))
                .isNotNull()
                .isNotPresent();
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    public void updatePasswordSuccess() {
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));


        String newPassword = "awesomePassword";
        assertThat(instance.setNewPassword(USER_LOGIN, newPassword))
                .isNotNull()
                .isPresent()
                .hasValue(user)
                .map(UserInfo::getPassword)
                .get()
                .isNotNull()
                .matches(p -> passwordEncoder.matches(newPassword, p), "Password and encrypted password are mismatch");

        verify(userRepository).findByLogin(USER_LOGIN);
    }
}