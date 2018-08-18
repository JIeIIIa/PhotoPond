package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        mockUser = new UserInfoBuilder().id(777L).login("mockUser").password("password").role(UserRole.USER).build();
    }

    @Test
    public void existsByLoginSuccess() {
        //Given
        when(userRepository.findByLogin(mockUser.getLogin()))
                .thenReturn(Optional.of(mockUser));

        //When
        assertThat(instance.existsByLogin(mockUser.getLogin()))
                .isTrue();

        //Then
        verify(userRepository).findByLogin(mockUser.getLogin());
    }

    @Test
    public void existByLoginFailure() {
        //Given
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        assertThat(instance.existsByLogin(USER_LOGIN))
                .isFalse();

        //Then
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    public void addUserSuccess() {
        //Given
        String password = "password";
        UserInfoDTO userDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN).password(password)
                .build();
        UserInfo user = new UserInfo(USER_LOGIN, password, UserRole.USER);

        //When
        instance.addUser(userDTO);

        //Then
        ArgumentCaptor<UserInfo> argument = ArgumentCaptor.forClass(UserInfo.class);
        verify(userRepository).saveAndFlush(argument.capture());
        assertThat(argument.getValue())
                .isEqualToIgnoringGivenFields(user, "password")
                .matches(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()), "Password and encrypted password are mismatch");
    }

    @Test
    public void AddNullAsUser() {
        instance.addUser(null);

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    public void findByLoginExistsUser() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, "qwerty123!", UserRole.USER);
        when(userRepository.findByLogin(USER_LOGIN))
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
        verify(userRepository).findByLogin(USER_LOGIN);
    }


    @Test
    public void findByLoginNotExistsUser() {
        //Given
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        Optional<UserInfoDTO> result = instance.findUserByLogin(USER_LOGIN);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
        verify(userRepository).findByLogin(USER_LOGIN);
        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    public void findByLoginWhenLoginIsNull() {
        //Given
        when(userRepository.findByLogin(null)).thenReturn(Optional.empty());

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
        final Long id = 321L;
        UserInfo user = new UserInfoBuilder().id(id).login(USER_LOGIN).password("qwerty123!").role(UserRole.USER).build();
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
        final Long id = 321L;
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
        UserInfoDTO[] expected = {
                UserInfoDTOBuilder.getInstance().id(1L).login(USER_LOGIN).password("qwerty123!").role(UserRole.USER).build(),
                UserInfoDTOBuilder.getInstance().id(2L).login("anotherUser").password("qwerty123!").role(UserRole.ADMIN).build()
        };

        when(userRepository.findAll()).thenReturn(
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
        String oldPassword = "password";
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN)
                .oldPassword(oldPassword)
                .password(newPassword)
                .passwordConfirmation(newPassword)
                .build();
        UserInfo user = new UserInfo(USER_LOGIN, passwordEncoder.encode(oldPassword), UserRole.USER);
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        boolean result = instance.setNewPassword(userInfoDTO);

        //Then
        assertThat(result).isTrue();

        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    public void updateWithPasswordNotNullSuccess() {
        //Given
        String newPassword = "newPassword";
        UserInfo newInformation = new UserInfoBuilder()
                .id(mockUser.getId())
                .login("user")
                .password(newPassword)
                .role(UserRole.ADMIN).build();
        UserInfo expected = new UserInfo().copyFrom(newInformation);
        expected.setPassword(passwordEncoder.encode(newInformation.getPassword()));

        when(userRepository.countByLoginAndIdNot(newInformation.getLogin(), newInformation.getId()))
                .thenReturn(0L);
        when(userRepository.findById(mockUser.getId()))
                .thenReturn(Optional.ofNullable(mockUser));
        when(userRepository.save(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

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
        long id = 777L;
        UserInfo newInformation = new UserInfoBuilder()
                .id(id)
                .login("user")
                .password(null)
                .role(UserRole.ADMIN).build();
        UserInfo expected = new UserInfo().copyFrom(newInformation);
        expected.setPassword(mockUser.getPassword());

        when(userRepository.countByLoginAndIdNot(newInformation.getLogin(), newInformation.getId()))
                .thenReturn(0L);
        when(userRepository.findById(mockUser.getId()))
                .thenReturn(Optional.ofNullable(mockUser));
        when(userRepository.save(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

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
        UserInfo newInformation = new UserInfoBuilder()
                .id(id)
                .login(userLogin)
                .password("newPassword")
                .role(UserRole.ADMIN).build();
        when(userRepository.countByLoginAndIdNot(userLogin, id)).thenReturn(1L);

        //When
        Optional<UserInfo> userAfterUpdate = instance.update(newInformation);

        //Then
        assertThat(userAfterUpdate).isNotPresent();
        verify(userRepository, never()).save(any(UserInfo.class));
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
        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    public void deleteExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(777L).login("user").password("qwerty123!").role(UserRole.USER).build();
        when(userRepository.findById(777L)).thenReturn(
                Optional.ofNullable(user)
        );

        //When
        Optional<UserInfo> deletedUser = instance.delete(777);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isPresent()
                .hasValue(user);
        verify(userRepository).delete(user);
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
        verify(userRepository, never()).deleteById(id);
        verify(userRepository, never()).delete(any(UserInfo.class));
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
        when(userRepository.findAllByRole(UserRole.USER))
                .thenReturn(asList(
                        user,
                        mockUser)
                );

        //When
        List<UserInfo> allUsers = instance.findAllByRole(UserRole.USER);

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(2)
                .contains(user, mockUser);
    }


    @Test
    public void findByRoleAllAdmin() {
        //Given
        UserInfo admin = new UserInfoBuilder().id(1L).login("ADMIN").password("qwerty123!").role(UserRole.ADMIN).build();
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