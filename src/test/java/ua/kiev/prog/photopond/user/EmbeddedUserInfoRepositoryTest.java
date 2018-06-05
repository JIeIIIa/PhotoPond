package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedUserInfoRepositoryTest {
    private static final String USER_LOGIN = "user";
    private EmbeddedUserInfoRepository embeddedUserInfoRepository;
    private UserInfo testUser = null;

    @Before
    public void setUp() {
        testUser = new UserInfo(USER_LOGIN, "user", UserRole.USER);
        UserInfo first = new UserInfo("someUser", "password", UserRole.ADMIN);
        UserInfo second = new UserInfo("admin", "admin", UserRole.DEACTIVATED);
        testUser.setId(1L);
        first.setId(2L);
        second.setId(3L);

        embeddedUserInfoRepository = new EmbeddedUserInfoRepository(new LinkedList<>(
                Arrays.asList(testUser, first, second))
        );
    }

    @Test
    public void existsByLogin() {
        assertThat(embeddedUserInfoRepository.existsByLogin(USER_LOGIN))
                .isTrue();
    }

    @Test
    public void notExistsByLogin() {
        assertThat(embeddedUserInfoRepository.existsByLogin("UnknownUser"))
                .isFalse();
    }

    @Test
    public void findUserByLogin() {
        assertThat(embeddedUserInfoRepository.findUserByLogin(USER_LOGIN))
                .isNotNull()
                .isPresent()
                .hasValue(testUser);
    }

    @Test
    public void findUserWithNullLogin() {
        assertThat(embeddedUserInfoRepository.findUserByLogin(null))
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void findUserByLoginFailure() {
        assertThat(embeddedUserInfoRepository.findUserByLogin("UnknownUser"))
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void addUser() throws AddToRepositoryException {
        //Given
        final String anotherUserLogin = "anotherUser";
        UserInfo anotherUser = new UserInfo(anotherUserLogin, "pass", UserRole.USER);

        //When
        embeddedUserInfoRepository.addUser(anotherUser);

        //Then
        assertThat(embeddedUserInfoRepository.existsByLogin(anotherUserLogin))
                .isTrue();
        assertThat(embeddedUserInfoRepository.findUserByLogin(anotherUserLogin))
                .isNotNull()
                .isPresent()
                .hasValue(anotherUser);
    }

    @Test(expected = AddToRepositoryException.class)
    public void addNullAsUser() throws AddToRepositoryException {
        //When
        embeddedUserInfoRepository.addUser(null);
    }

    @Test
    public void findAllUsers() {
        //When
        List<UserInfo> allUsers = embeddedUserInfoRepository.findAllUsers();

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(3);
    }

    @Test
    public void deleteExistsUser() {
        //Given
        Long id = testUser.getId();

        //When
        embeddedUserInfoRepository.delete(id);

        //Then
        assertThat(embeddedUserInfoRepository.findUserByLogin(USER_LOGIN))
                .isNotPresent();
        assertThat(embeddedUserInfoRepository.findAllUsers())
                .hasSize(2);
    }

    @Test
    public void deleteNotExistsUser() {
        //Given
        Long id = -123L;

        //When
        embeddedUserInfoRepository.delete(id);

        //Then
        assertThat(embeddedUserInfoRepository.findUserByLogin(USER_LOGIN))
                .isPresent()
                .hasValue(testUser);
        assertThat(embeddedUserInfoRepository.findAllUsers())
                .hasSize(3);
    }

    @Test
    public void updateExistsUser() {
        //Given
        testUser.setLogin("qwertyLogin");
        testUser.setPassword("aassddffgg");
        testUser.setRole(UserRole.DEACTIVATED);
        UserInfo expected = new UserInfo().copyFrom(testUser);

        //When
        UserInfo result = embeddedUserInfoRepository.update(testUser);

        //Then
        assertThat(result).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void updateNotExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(-123L).login("phantomUser").password("Password").build();
        //When
        UserInfo result = embeddedUserInfoRepository.update(user);

        //Then
        assertThat(result).isNull();
    }

    @Test
    public void findByIdSuccess() {
        //When
        Optional<UserInfo> result = embeddedUserInfoRepository.findById(testUser.getId());

        //Then
        assertThat(result)
                .isPresent()
                .hasValue(testUser);
    }

    @Test
    public void findByIdFailure() {
        //When
        Optional<UserInfo> result = embeddedUserInfoRepository.findById(-123L);

        //Then
        assertThat(result)
                .isNotPresent();
    }

    @Test
    public void findAllByRole() {
        //When
        List<UserInfo> result = embeddedUserInfoRepository.findAllByRole(UserRole.USER);

        //Then
        assertThat(result)
                .isNotNull()
                .containsExactlyInAnyOrder(testUser);
    }

    @Test
    public void countByRole() {
        //When
        Long count = embeddedUserInfoRepository.countByRole(UserRole.ADMIN);

        //Then
        assertThat(count).isEqualTo(1);
    }
}

