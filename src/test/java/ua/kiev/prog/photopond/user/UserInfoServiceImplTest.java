package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
    public void addUserSuccess() {
        //Given
        String password = "password";
        UserInfoDTO userDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN).password(password).role(UserRole.USER)
                .build();
        UserInfo user = new UserInfo(USER_LOGIN, password, UserRole.USER);

        //When
        instance.addUser(userDTO);

        //Then
        ArgumentCaptor<UserInfo> argument = ArgumentCaptor.forClass(UserInfo.class);
        verify(userRepository).addUser(argument.capture());
        assertThat(argument.getValue())
                .isEqualToIgnoringGivenFields(user, "password")
                .matches(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()), "Password and encrypted password are mismatch");
    }

    @Test
    public void AddNullAsUser() {
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
        UserInfoDTO expected = UserInfoMapper.toDto(user);

        //When
        Optional<UserInfoDTO> result = instance.findUserByLogin(USER_LOGIN);

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .get()
                .isEqualToComparingFieldByField(expected);
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }


    @Test
    public void findByLoginNotExistsUser() {
        //Given
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        Optional<UserInfoDTO> result = instance.findUserByLogin(USER_LOGIN);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    public void findByLoginWhenLoginIsNull() {
        //Given
        when(userRepository.findUserByLogin(null)).thenReturn(Optional.empty());

        //When
        Optional<UserInfoDTO> user = instance.findUserByLogin(null);

        //Then
        assertThat(user)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void findByIdExistUser() {
        //Given
        final long id = 123L;
        UserInfo user = new UserInfoBuilder().id(id).login(USER_LOGIN).password("password").role(UserRole.USER).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .id(id).login(user.getLogin()).password(user.getPassword()).role(user.getRole()).build();
        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        //When
        Optional<UserInfoDTO> result = instance.findById(id);

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .get()
                .isEqualToComparingFieldByField(expected);
        verify(userRepository).findById(id);
    }


    @Test
    public void findByIdNotExistsUser() {
        //Given
        final long id = 123L;
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        //When
        Optional<UserInfoDTO> result = instance.findById(id);

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
        UserInfoDTO[] expected = {
                UserInfoDTOBuilder.getInstance().id(1L).login(USER_LOGIN).password("qwerty123!").role(UserRole.USER).build(),
                UserInfoDTOBuilder.getInstance().id(2L).login("anotherUser").password("qwerty123!").role(UserRole.ADMIN).build()
        };

        when(userRepository.findAllUsers()).thenReturn(
                asList(user, anotherUser)
        );

        //When
        List<UserInfoDTO> allUsers = instance.findAllUsers();

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(2)
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(expected);
    }

    @Test
    public void updatePasswordSuccess() {
        //Given
        String newPassword = "awesomePassword";
        String oldPassword = "somePassword";
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN)
                .oldPassword(oldPassword)
                .password(newPassword)
                .passwordConfirmation(newPassword)
                .build();

        UserInfo user = new UserInfo(USER_LOGIN, passwordEncoder.encode(oldPassword), UserRole.USER);
        UserInfo expectedUser = new UserInfo(USER_LOGIN, passwordEncoder.encode(newPassword), UserRole.USER);

        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        boolean result = instance.setNewPassword(userInfoDTO);

        //Then
        assertThat(result).isTrue();

        verify(userRepository).findUserByLogin(USER_LOGIN);

        ArgumentCaptor<UserInfo> captor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userRepository).update(captor.capture());
        assertThat(captor.getValue())
                .isNotNull()
                .isEqualToIgnoringGivenFields(expectedUser, "password")
                .matches(u -> passwordEncoder.matches(newPassword, u.getPassword()), "Password and encrypted password are mismatch");
    }

    @Test
    public void updateWithPasswordNotNullSuccess() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, "somePassword", UserRole.USER);
        String newPassword = "newPassword";
        UserInfo newInformation = new UserInfoBuilder()
                .login("newLogin")
                .password(newPassword)
                .role(UserRole.ADMIN).build();
        UserInfo expected = new UserInfo().copyFrom(newInformation);
        expected.setPassword(passwordEncoder.encode(newInformation.getPassword()));

        when(userRepository.existsByLogin(newInformation.getLogin(), newInformation.getId()))
                .thenReturn(false);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.update(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> {
                    UserInfo u = (UserInfo) invocationOnMock.getArguments()[0];
                    if (u.getId() == user.getId()) {
                        return user.copyFrom(u);
                    } else {
                        return u;
                    }
                });

        //When
        Optional<UserInfo> result = instance.update(newInformation);

        //Then
        assertThat(result)
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(expected, "password")
                .matches(u -> passwordEncoder.matches(newPassword, u.getPassword()));
    }

    @Test
    public void updateWithPasswordIsNullSuccess() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, "somePassword", UserRole.USER);
        UserInfo newInformation = new UserInfoBuilder()
                .login("user")
                .password(null)
                .role(UserRole.ADMIN).build();
        UserInfo expected = new UserInfo().copyFrom(newInformation);
        expected.setPassword(user.getPassword());

        when(userRepository.existsByLogin(newInformation.getLogin(), newInformation.getId()))
                .thenReturn(false);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(userRepository.update(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> {
                    UserInfo u = (UserInfo) invocationOnMock.getArguments()[0];
                    if (u.getId() == user.getId()) {
                        return user.copyFrom(u);
                    } else {
                        return u;
                    }
                });

        //When
        Optional<UserInfo> result = instance.update(newInformation);

        //Then
        assertThat(result)
                .isPresent()
                .hasValue(expected);
    }

    @Test
    public void updateWhenExistsSameLogin() {
        //Given
        String userLogin = "user";
        long id = 777L;
        UserInfo user = new UserInfoBuilder()
                .id(id)
                .login("oldUser")
                .password("password")
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
