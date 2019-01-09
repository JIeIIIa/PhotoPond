package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
class EmbeddedUserInfoRepositoryTest {
    private static final String USER_LOGIN = "user";
    private EmbeddedUserInfoRepository embeddedUserInfoRepository;
    private UserInfo testUser = null;

    @BeforeEach
    void setUp() {
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
    void existsByLogin() {
        assertThat(embeddedUserInfoRepository.existsByLogin(USER_LOGIN))
                .isTrue();
    }

    @Test
    void notExistsByLogin() {
        assertThat(embeddedUserInfoRepository.existsByLogin("UnknownUser"))
                .isFalse();
    }

    @Test
    void existsByLoginExceptId() {
        assertThat(embeddedUserInfoRepository.existsByLogin(USER_LOGIN, 2L))
                .isTrue();
    }

    @Test
    void notExistsByLoginExceptId() {
        assertThat(embeddedUserInfoRepository.existsByLogin(USER_LOGIN, 1L))
                .isFalse();
    }

    @Test
    void findUserByLogin() {
        assertThat(embeddedUserInfoRepository.findUserByLogin(USER_LOGIN))
                .isNotNull()
                .isPresent()
                .hasValue(testUser);
    }

    @Test
    void findUserWithNullLogin() {
        assertThat(embeddedUserInfoRepository.findUserByLogin(null))
                .isNotNull()
                .isNotPresent();
    }

    @Test
    void findUserByLoginFailure() {
        assertThat(embeddedUserInfoRepository.findUserByLogin("UnknownUser"))
                .isNotNull()
                .isNotPresent();
    }

    @Test
    void addUser() throws AddToRepositoryException {
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

    @Test
    void addNullAsUser() throws AddToRepositoryException {
        //When
        assertThrows(AddToRepositoryException.class,
                () -> embeddedUserInfoRepository.addUser(null)
        );
    }

    @Test
    void findAllUsers() {
        //When
        List<UserInfo> allUsers = embeddedUserInfoRepository.findAllUsers();

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(3);
    }

    @Test
    void deleteExistsUser() {
        //Given
        long id = testUser.getId();

        //When
        embeddedUserInfoRepository.delete(id);

        //Then
        assertThat(embeddedUserInfoRepository.findUserByLogin(USER_LOGIN))
                .isNotPresent();
        assertThat(embeddedUserInfoRepository.findAllUsers())
                .hasSize(2);
    }

    @Test
    void deleteNotExistsUser() {
        //Given
        long id = -123L;

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
    void updateExistsUser() {
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
    void updateNotExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(-123L).login("phantomUser").password("Password").build();
        //When
        UserInfo result = embeddedUserInfoRepository.update(user);

        //Then
        assertThat(result).isNull();
    }

    @Test
    void findByIdSuccess() {
        //When
        Optional<UserInfo> result = embeddedUserInfoRepository.findById(testUser.getId());

        //Then
        assertThat(result)
                .isPresent()
                .hasValue(testUser);
    }

    @Test
    void findByIdFailure() {
        //When
        Optional<UserInfo> result = embeddedUserInfoRepository.findById(-123L);

        //Then
        assertThat(result)
                .isNotPresent();
    }

    @Test
    void findAllByRole() {
        //When
        List<UserInfo> result = embeddedUserInfoRepository.findAllByRole(UserRole.USER);

        //Then
        assertThat(result)
                .isNotNull()
                .containsExactlyInAnyOrder(testUser);
    }

    @Test
    void countByRole() {
        //When
        Long count = embeddedUserInfoRepository.countByRole(UserRole.ADMIN);

        //Then
        assertThat(count).isEqualTo(1);
    }
}

