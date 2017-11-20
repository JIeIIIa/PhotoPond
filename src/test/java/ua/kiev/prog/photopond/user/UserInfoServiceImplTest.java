package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoServiceImplTest {

    @Mock
    private UserInfoSimpleRepository userRepository;

    private UserInfoServiceImpl instance;

    private final String USER_LOGIN = "user";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        instance = new UserInfoServiceImpl(userRepository);
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
    public void failureExistUserByLogin() throws Exception {
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
        doThrow(new AddToRepositoryException()).when(userRepository).addUser(eq(nullUser));

        instance.addUser(null);

        verify(userRepository).addUser(eq(nullUser));
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
