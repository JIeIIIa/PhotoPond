package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedUserInfoRepositoryTest {
    private static final String USER_LOGIN = "user";
    private EmbeddedUserInfoRepository embeddedUserInfoRepository;
    private UserInfo testUser = null;

    @Before
    public void setUp() {
        testUser = new UserInfo(USER_LOGIN, "user", UserRole.USER);
        UserInfo first = new UserInfo("someUser", "password", UserRole.USER);
        UserInfo second = new UserInfo("admin", "admin", UserRole.USER);

        embeddedUserInfoRepository = new EmbeddedUserInfoRepository(Arrays.asList(testUser, first, second));
    }

    @Test
    public void checkUserByIDSuccessTest() {
        assertThat(embeddedUserInfoRepository.existByLogin(USER_LOGIN))
                .isTrue();
    }

    @Test
    public void notExistUserTest() {
        assertThat(embeddedUserInfoRepository.existByLogin("UnknownUser"))
                .isFalse();
    }

    @Test
    public void findUserSuccessTest() {
        assertThat(embeddedUserInfoRepository.findByLogin(USER_LOGIN))
                .isNotNull()
                .isPresent()
                .hasValue(testUser);
    }

    @Test
    public void findUserWithNullLoginTest() {
        assertThat(embeddedUserInfoRepository.findByLogin(null))
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void findUserFailureTest() {
        assertThat(embeddedUserInfoRepository.findByLogin("UnknownUser"))
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void addUserTest() throws AddToRepositoryException {
        final String anotherUserLogin = "anotherUser";
        UserInfo anotherUser = new UserInfo(anotherUserLogin, "pass", UserRole.USER);

        embeddedUserInfoRepository.addUser(anotherUser);

        assertThat(embeddedUserInfoRepository.existByLogin(anotherUserLogin))
                .isTrue();
        assertThat(embeddedUserInfoRepository.findByLogin(anotherUserLogin))
                .isNotNull()
                .isPresent()
                .hasValue(anotherUser);
    }

    @Test(expected = AddToRepositoryException.class)
    public void addNullAsUserTest() throws AddToRepositoryException {
        embeddedUserInfoRepository.addUser(null);
    }
}

