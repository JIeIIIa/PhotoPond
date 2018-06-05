package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
    public void existUserByLoginSuccess() {
        //Given
        Mockito.when(userRepository.existsByLogin(USER_LOGIN))
                .thenReturn(true);

        //When
        assertThat(instance.existsByLogin(USER_LOGIN))
                .isTrue();

        //Then
        verify(userRepository).existsByLogin(USER_LOGIN);
    }

    @Test
    public void existUserByLoginFailure() {
        //Given
        Mockito.when(userRepository.existsByLogin(USER_LOGIN))
                .thenReturn(false);

        //When
        assertThat(instance.existsByLogin(USER_LOGIN))
                .isFalse();

        //Then
        verify(userRepository).existsByLogin(USER_LOGIN);
    }

    @Test
    public void addUserSuccess() throws Exception {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);

        //When
        instance.addUser(user);

        //Then
        verify(userRepository).addUser(eq(user));
    }

    @Test
    public void AddNullAsUser() throws Exception {
        //When
        instance.addUser(null);

        //Then
        verify(userRepository, never()).addUser(any(UserInfo.class));
    }

    @Test
    public void findByLoginExistUser() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, "password", UserRole.USER);
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        Optional<UserInfo> result = instance.findUserByLogin(USER_LOGIN);

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(user);
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }


    @Test
    public void findByLoginNotExistsUser() {
        //Given
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        Optional<UserInfo> result = instance.findUserByLogin(USER_LOGIN);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    public void findByIdExistUser() {
        //Given
        final Long id = 123L;
        UserInfo user = new UserInfoBuilder().id(id).login(USER_LOGIN).password("password").role(UserRole.USER).build();
        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        //When
        Optional<UserInfo> result = instance.findById(id);

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(user);
        verify(userRepository).findById(id);
    }


    @Test
    public void findByIdNotExistsUser() {
        //Given
        final Long id = 123L;
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        //When
        Optional<UserInfo> result = instance.findById(id);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
        verify(userRepository).findById(id);
    }

    @Test
    public void findAllUsers() {
        //Given
        UserInfo user = new UserInfoBuilder().id(1L).login(USER_LOGIN).password("qwerty123!").role(UserRole.USER).build();
        UserInfo anotherUser = new UserInfoBuilder().id(2L).login("anotherUser").password("qwerty123!").role(UserRole.ADMIN).build();
        when(userRepository.findAllUsers()).thenReturn(
                asList(user, anotherUser)
        );

        //When
        List<UserInfo> allUsers = instance.findAllUsers();

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(user, anotherUser);
    }

    @Test
    public void updatePasswordSuccess() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, "somePassword", UserRole.USER);
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));
        String newPassword = "awesomePassword";

        //When
        Optional<UserInfo> result = instance.setNewPassword(USER_LOGIN, newPassword);

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(user)
                .map(UserInfo::getPassword)
                .get()
                .isNotNull()
                .matches(p -> passwordEncoder.matches(newPassword, p), "Password and encrypted password are mismatch");

        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    public void updateWhenExistsSameLogin() {
        //Given
        String userLogin = "user";
        long id = 777L;
        UserInfo user = new UserInfoBuilder()
                .id(id)
                .login("oldUser")
                .password("oldPassword")
                .role(UserRole.USER).build();
        UserInfo newInformation = new UserInfoBuilder()
                .id(id)
                .login(userLogin)
                .password("newPassword")
                .role(UserRole.ADMIN).build();
        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
        when(userRepository.existsByLogin(userLogin, id)).thenReturn(true);

        //When
        Optional<UserInfo> userAfterUpdate = instance.update(newInformation);

        //Then
        assertThat(userAfterUpdate).isNotPresent();
        verify(userRepository, never()).update(any(UserInfo.class));
    }

    @Test
    public void updateWithWrongId() {
        //Given
        long id = 101010;
        UserInfo newInformation = new UserInfoBuilder()
                .id(id)
                .login("newUser")
                .password("password")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        //When
        Optional<UserInfo> userAfterUpdate = instance.update(newInformation);

        //Then
        assertThat(userAfterUpdate).isNotPresent();
        verify(userRepository, never()).update(any(UserInfo.class));
    }


    @Test
    public void deleteExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(777L).login("user").password("qwerty123!").role(UserRole.USER).build();
        when(userRepository.findById(777L)).thenReturn(
                Optional.ofNullable(user)
        );

        //When
        Optional<UserInfo> deletedUser = instance.delete(777L);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isPresent()
                .hasValue(user);
        verify(userRepository).delete(777L);
    }

    @Test
    public void deleteWithFailureId() {
        //Given
        long id = 101010L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        //When
        Optional<UserInfo> deletedUser = instance.delete(id);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isNotPresent();
        verify(userRepository, never()).delete(id);
    }

    @Test
    public void existsUserWithAdminRole() {
        //Given
        when(userRepository.countByRole(UserRole.ADMIN))
                .thenReturn(1L);

        //When
        boolean isExists = instance.existsWithAdminRole();

        //Then
        assertThat(isExists).isTrue();
    }

    @Test
    public void notExistsUserWithAdminRole() {
        //Given
        when(userRepository.countByRole(UserRole.ADMIN))
                .thenReturn(0L);

        //When
        boolean isExists = instance.existsWithAdminRole();

        //Then
        assertThat(isExists).isFalse();
    }

    @Test
    public void findByRoleAllUsers() {
        //Given
        UserInfo user = new UserInfoBuilder().id(1L).login("awesomeUser").password("qwerty123!").role(UserRole.USER).build();
        UserInfo anotherUser = new UserInfoBuilder().id(2L).login("someUser").password("qwerty123!").role(UserRole.USER).build();
        when(userRepository.findAllByRole(UserRole.USER))
                .thenReturn(asList(
                        user,
                        anotherUser)
                );

        //When
        List<UserInfo> allUsers = instance.findAllByRole(UserRole.USER);

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(2)
                .contains(user, anotherUser);
    }

    @Test
    public void findByRoleAllAdmin() {
        //Given
        UserInfo admin = new UserInfoBuilder().id(1L).login("Administrator").password("qwerty123!").role(UserRole.ADMIN).build();
        UserInfo expectedUser = new UserInfo().copyFrom(admin);
        when(userRepository.findAllByRole(UserRole.ADMIN))
                .thenReturn(singletonList(admin));

        //When
        List<UserInfo> allAdmin = instance.findAllByRole(UserRole.ADMIN);

        //Then
        assertThat(allAdmin)
                .isNotNull()
                .hasSize(1)
                .containsExactly(expectedUser);
    }
}
