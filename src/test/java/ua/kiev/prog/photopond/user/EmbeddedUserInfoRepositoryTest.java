package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class EmbeddedUserInfoRepositoryTest {
    private static final String USER_LOGIN = "user";
    private EmbeddedUserInfoRepository embeddedUserInfoRepository;
    private UserInfo testUser = null;

    @Before
    public void setUp() throws Exception {
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
                .isEqualTo(testUser)
        ;
    }

    @Test
    public void findUserWithNullLoginTest() {
        assertThat(embeddedUserInfoRepository.findByLogin(null))
                .isNull();
    }

    @Test
    public void findUserFailureTest() {
        assertThat(embeddedUserInfoRepository.findByLogin("UnknownUser"))
                .isNull();
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
                .isEqualTo(anotherUser);
    }

    @Test(expected = AddToRepositoryException.class)
    public void addNullAsUserTest() throws AddToRepositoryException {
        embeddedUserInfoRepository.addUser(null);
    }
}

