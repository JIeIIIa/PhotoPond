package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoServiceImplTest {

    @Mock
    private UserInfoSimpleRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private UserInfoServiceImpl instance;

    private final String USER_LOGIN = "user";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        instance = new UserInfoServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    public void successExistUserByLogin() {
        Mockito.when(userRepository.existByLogin(USER_LOGIN))
                .thenReturn(true);

        assertThat(instance.existByLogin(USER_LOGIN))
                .isTrue();
        verify(userRepository).existByLogin(USER_LOGIN);
    }

    @Test
    public void failureExistUserByLogin() {
        Mockito.when(userRepository.existByLogin(USER_LOGIN))
                .thenReturn(false);

        assertThat(instance.existByLogin(USER_LOGIN))
                .isFalse();
        verify(userRepository).existByLogin(USER_LOGIN);
    }

    @Test
    public void successAddUserTest() throws Exception {
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);

        instance.addUser(user);

        verify(userRepository).addUser(eq(user));
    }

    @Test
    public void AddNullAsUserTest() throws Exception {
        UserInfo nullUser = null;

        instance.addUser(null);

        verify(userRepository, never()).addUser(eq(nullUser));
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
        UserInfo user = new UserInfo(USER_LOGIN, "somePassword", UserRole.USER);
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
