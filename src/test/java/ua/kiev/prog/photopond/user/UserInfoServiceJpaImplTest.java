package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoServiceJpaImplTest {

    @Mock
    private UserInfoJpaRepository userRepository;

    private UserInfoServiceJpaImpl instance;

    private final String USER_LOGIN = "user";

    private UserInfo mockUser;

    @Before
    public void setUp() throws Exception {
        instance = new UserInfoServiceJpaImpl(userRepository);
        mockUser = new UserInfoBuilder().id(777).login("mockUser").password("password").role(UserRole.USER).build();
    }

    @Test
    public void successExistUserByLogin() {
        Mockito.when(userRepository.findByLogin(mockUser.getLogin()))
                .thenReturn(mockUser);

        assertThat(instance.existByLogin(mockUser.getLogin()))
                .isTrue();
        verify(userRepository).findByLogin(mockUser.getLogin());
    }

    @Test
    public void failureExistUserByLogin() throws Exception {
        Mockito.when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(null);

        assertThat(instance.existByLogin(USER_LOGIN))
                .isFalse();
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    public void successAddUserTest() throws Exception {
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);

        instance.addUser(user);

        verify(userRepository).save(eq(user));
    }

    @Test
    public void AddNullAsUserTest() throws Exception {
        UserInfo nullUser = null;

        instance.addUser(nullUser);

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    public void findExistUserByLogin() throws Exception {
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(user);

        assertThat(instance.getUserByLogin(USER_LOGIN))
                .isNotNull()
                .isEqualTo(user);
        verify(userRepository).findByLogin(USER_LOGIN);
    }


    @Test
    public void findNotExistUserByLogin() throws Exception {

        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(null);

        assertThat(instance.getUserByLogin(USER_LOGIN))
                .isNull();
        verify(userRepository).findByLogin(USER_LOGIN);
    }
}