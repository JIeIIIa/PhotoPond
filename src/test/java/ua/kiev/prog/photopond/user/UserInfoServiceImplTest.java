package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
class UserInfoServiceImplTest {

    @Mock
    private UserInfoSimpleRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private UserInfoServiceImpl instance;

    private final String USER_LOGIN = "user";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        instance = new UserInfoServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void existUserByLoginSuccess() {
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
    void existUserByLoginFailure() {
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
    void addUserSuccess() {
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
    void AddNullAsUser() {
        //When
        instance.addUser(null);

        //Then
        verify(userRepository, never()).addUser(any(UserInfo.class));
    }

    @Test
    void findByLoginExistUser() {
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
    void findByLoginNotExistsUser() {
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
    void findByLoginWhenLoginIsNull() {
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
    void findByIdExistUser() {
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
    void findByIdNotExistsUser() {
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
    void findAllUsers() {
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
    void updatePasswordSuccess() {
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
    void resetPasswordSuccess() {
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
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        boolean result = instance.resetPassword(userInfoDTO.getLogin(), userInfoDTO.getPassword());

        //Then
        assertThat(result).isTrue();

        verify(userRepository).findUserByLogin(USER_LOGIN);
    }


    @Test
    void resetPasswordFailure() {
        //Given
        String newPassword = "awesomePassword";
        String oldPassword = "password";
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN)
                .oldPassword(oldPassword)
                .password(newPassword)
                .passwordConfirmation(newPassword)
                .build();
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        boolean result = instance.resetPassword(userInfoDTO.getLogin(), userInfoDTO.getPassword());

        //Then
        assertThat(result).isFalse();

        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    void updateAvatarSuccess() {
        //Given
        String filename = "avatar.jpg";
        MockMultipartFile file = new MockMultipartFile("avatar", filename, "img/jpg", filename.getBytes());
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN)
                .avatar(file)
                .build();
        UserInfo user = new UserInfo(USER_LOGIN, passwordEncoder.encode("password"), UserRole.USER);
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));
        when(userRepository.update(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        //When
        boolean result = instance.updateAvatar(userInfoDTO);

        //Then
        assertThat(result).isTrue();
        verify(userRepository).findUserByLogin(USER_LOGIN);
        assertThat(user.getAvatar()).isEqualTo(filename.getBytes());
    }


    @Test
    void updateAvatarFailure() {
        //Given
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN)
                .build();
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        boolean result = instance.updateAvatar(userInfoDTO);

        //Then
        assertThat(result).isFalse();
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    void retrieveAvatar() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, passwordEncoder.encode("password"), UserRole.USER);
        String avatarString = "someAvatarString";
        user.setAvatar(avatarString.getBytes());
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        byte[] result = instance.retrieveAvatar(USER_LOGIN);

        //Then
        assertThat(result).isEqualTo(avatarString.getBytes());
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    void retrieveAvatarWithDefaultValue() {
        //Given
        when(userRepository.findUserByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        byte[] result = instance.retrieveAvatar(USER_LOGIN);

        //Then
        assertThat(result).isEqualTo(new byte[0]);
        verify(userRepository).findUserByLogin(USER_LOGIN);
    }

    @Test
    void updateWithPasswordNotNullSuccess() {
        //Given
        UserInfo user = new UserInfoBuilder()
                .id(123L)
                .login(USER_LOGIN)
                .password("somePassword")
                .role(UserRole.USER)
                .build();
        String newPassword = "newPassword";
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(123L)
                .login("newLogin")
                .password(newPassword)
                .role(UserRole.ADMIN).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .source(newInformation)
                .password(user.getPassword())
                .build();

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
        Optional<UserInfoDTO> result = instance.updateBaseInformation(newInformation);

        //Then
        assertThat(result)
                .isPresent()
                .get()
                .isEqualToComparingFieldByField(expected);
    }

    @Test
    void updateWithPasswordIsNullSuccess() {
        //Given
        UserInfo user = new UserInfoBuilder()
                .id(123L)
                .login(USER_LOGIN)
                .password("somePassword")
                .role(UserRole.USER)
                .build();
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(123L)
                .login("user")
                .password(null)
                .role(UserRole.ADMIN).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .source(newInformation)
                .password(user.getPassword())
                .build();

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
        Optional<UserInfoDTO> result = instance.updateBaseInformation(newInformation);

        //Then
        assertThat(result)
                .isPresent()
                .get()
                .isEqualToComparingFieldByField(expected);
    }

    @Test
    void updateWhenExistsSameLogin() {
        //Given
        String userLogin = "user";
        long id = 777L;
        UserInfo user = new UserInfoBuilder()
                .id(id)
                .login("oldUser")
                .password("password")
                .role(UserRole.USER).build();
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(id)
                .login(userLogin)
                .password("newPassword")
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
        when(userRepository.existsByLogin(userLogin, id)).thenReturn(true);

        //When
        Optional<UserInfoDTO> userAfterUpdate = instance.updateBaseInformation(newInformation);

        //Then
        assertThat(userAfterUpdate).isNotPresent();
        verify(userRepository, never()).update(any(UserInfo.class));
    }

    @Test
    void updateWithWrongId() {
        //Given
        long id = 101010;
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(id)
                .login("newUser")
                .password("password")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        //When
        Optional<UserInfoDTO> userAfterUpdate = instance.updateBaseInformation(newInformation);

        //Then
        assertThat(userAfterUpdate).isNotPresent();
        verify(userRepository, never()).update(any(UserInfo.class));
    }


    @Test
    void deleteExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(777L).login("user").password("qwerty123!").role(UserRole.USER).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .id(777L)
                .login("user")
                .password("qwerty123!")
                .role(UserRole.USER)
                .build();
        when(userRepository.findById(777L)).thenReturn(
                Optional.ofNullable(user)
        );

        //When
        Optional<UserInfoDTO> deletedUser = instance.delete(777L);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isPresent()
                .get()
                .isEqualToComparingFieldByField(expected);
        verify(userRepository).delete(777L);
    }

    @Test
    void deleteWithFailureId() {
        //Given
        long id = 101010L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        //When
        Optional<UserInfoDTO> deletedUser = instance.delete(id);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isNotPresent();
        verify(userRepository, never()).delete(id);
    }

    @Test
    void existsUserWithAdminRole() {
        //Given
        when(userRepository.countByRole(UserRole.ADMIN))
                .thenReturn(1L);

        //When
        boolean isExists = instance.existsWithAdminRole();

        //Then
        assertThat(isExists).isTrue();
    }

    @Test
    void notExistsUserWithAdminRole() {
        //Given
        when(userRepository.countByRole(UserRole.ADMIN))
                .thenReturn(0L);

        //When
        boolean isExists = instance.existsWithAdminRole();

        //Then
        assertThat(isExists).isFalse();
    }

    @Test
    void findByRoleAllUsers() {
        //Given
        UserInfo user = new UserInfoBuilder().id(1L).login("awesomeUser").password("qwerty123!").role(UserRole.USER).build();
        UserInfo anotherUser = new UserInfoBuilder().id(2L).login("someUser").password("qwerty123!").role(UserRole.USER).build();
        UserInfoDTO[] expectedUsers = {
                UserInfoDTOBuilder.getInstance()
                        .id(1L).login("awesomeUser").password("qwerty123!").role(UserRole.USER).build(),
                UserInfoDTOBuilder.getInstance()
                        .id(2L).login("someUser").password("qwerty123!").role(UserRole.USER).build()
        };

        when(userRepository.findAllByRole(UserRole.USER))
                .thenReturn(asList(
                        user,
                        anotherUser)
                );

        //When
        List<UserInfoDTO> allUsers = instance.findAllByRole(UserRole.USER);

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(2)
                .usingFieldByFieldElementComparator()
                .contains(expectedUsers);
    }

    @Test
    void findByRoleAllAdmin() {
        //Given
        UserInfo admin = new UserInfoBuilder().id(1L).login("Administrator").password("qwerty123!").role(UserRole.ADMIN).build();
        UserInfoDTO expectedUser = UserInfoDTOBuilder.getInstance()
                .id(1L).login("Administrator").password("qwerty123!").role(UserRole.ADMIN).build();
        when(userRepository.findAllByRole(UserRole.ADMIN))
                .thenReturn(singletonList(admin));

        //When
        List<UserInfoDTO> allAdmin = instance.findAllByRole(UserRole.ADMIN);

        //Then
        assertThat(allAdmin)
                .isNotNull()
                .hasSize(1)
                .usingFieldByFieldElementComparator()
                .containsExactly(expectedUser);
    }
}
