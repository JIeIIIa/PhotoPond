package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
class UserInfoServiceJpaImplTest {

    @Mock
    private UserInfoJpaRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private UserInfoServiceJpaImpl instance;

    private final String USER_LOGIN = "user";

    private UserInfo mockUser;

    private UserInfoDTO mockUserDTO;

    UserInfoServiceJpaImplTest() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @BeforeEach
    void setUp() {
        instance = new UserInfoServiceJpaImpl(userRepository, passwordEncoder);
        mockUser = new UserInfoBuilder().id(777L).login("mockUser").password("password").role(UserRole.USER).build();
        mockUserDTO = UserInfoMapper.toDto(mockUser);
    }

    @Test
    void existsByLoginSuccess() {
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
    void existByLoginFailure() {
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
    void addUserSuccess() {
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
    void AddNullAsUser() {
        instance.addUser(null);

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    void findByLoginExistsUser() {
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
    void findByLoginNotExistsUser() {
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
    void findByLoginWhenLoginIsNull() {
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
    void findByIdExistUser() {
        //Given
        final Long id = 321L;
        UserInfo user = new UserInfoBuilder().id(id).login(USER_LOGIN).password("qwerty123!").role(UserRole.USER).build();
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
        final Long id = 321L;
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
    void updatePasswordSuccess() {
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
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        boolean result = instance.resetPassword(userInfoDTO.getLogin(), userInfoDTO.getPassword());

        //Then
        assertThat(result).isTrue();

        verify(userRepository).findByLogin(USER_LOGIN);
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
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        boolean result = instance.resetPassword(userInfoDTO.getLogin(), userInfoDTO.getPassword());

        //Then
        assertThat(result).isFalse();

        verify(userRepository).findByLogin(USER_LOGIN);
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
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        //When
        boolean result = instance.updateAvatar(userInfoDTO);

        //Then
        assertThat(result).isTrue();
        verify(userRepository).findByLogin(USER_LOGIN);
        assertThat(user.getAvatar()).isEqualTo(filename.getBytes());
    }


    @Test
    void updateAvatarFailure() {
        //Given
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(USER_LOGIN)
                .build();
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        boolean result = instance.updateAvatar(userInfoDTO);

        //Then
        assertThat(result).isFalse();
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    void retrieveAvatar() {
        //Given
        UserInfo user = new UserInfo(USER_LOGIN, passwordEncoder.encode("password"), UserRole.USER);
        String avatarString = "someAvatarString";
        user.setAvatar(avatarString.getBytes());
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.of(user));

        //When
        byte[] result = instance.retrieveAvatar(USER_LOGIN);

        //Then
        assertThat(result).isEqualTo(avatarString.getBytes());
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    void retrieveAvatarWithDefaultValue() {
        //Given
        String avatarString = "someAvatarString";
        instance.setDefaultAvatar(avatarString.getBytes());
        when(userRepository.findByLogin(USER_LOGIN))
                .thenReturn(Optional.empty());

        //When
        byte[] result = instance.retrieveAvatar(USER_LOGIN);

        //Then
        assertThat(result).isEqualTo(avatarString.getBytes());
        verify(userRepository).findByLogin(USER_LOGIN);
    }

    @Test
    void updateWithPasswordNotNullSuccess() {
        //Given
        String newPassword = "newPassword";
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(mockUser.getId())
                .login("user")
                .password(newPassword)
                .role(UserRole.ADMIN).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .source(newInformation)
                .password(mockUser.getPassword())
                .build();

        when(userRepository.countByLoginAndIdNot(newInformation.getLogin(), newInformation.getId()))
                .thenReturn(0L);
        when(userRepository.findById(mockUser.getId()))
                .thenReturn(Optional.ofNullable(mockUser));
        when(userRepository.saveAndFlush(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

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
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(mockUser.getId())
                .login("user")
                .password(null)
                .role(UserRole.ADMIN).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .source(newInformation)
                .password(mockUser.getPassword())
                .build();

        when(userRepository.countByLoginAndIdNot(newInformation.getLogin(), newInformation.getId()))
                .thenReturn(0L);
        when(userRepository.findById(mockUser.getId()))
                .thenReturn(Optional.ofNullable(mockUser));
        when(userRepository.saveAndFlush(any(UserInfo.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

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
        UserInfoDTO newInformation = UserInfoDTOBuilder.getInstance()
                .id(id)
                .login(userLogin)
                .password("newPassword")
                .role(UserRole.ADMIN).build();
        when(userRepository.countByLoginAndIdNot(userLogin, id)).thenReturn(1L);

        //When
        Optional<UserInfoDTO> userAfterUpdate = instance.updateBaseInformation(newInformation);

        //Then
        assertThat(userAfterUpdate).isNotPresent();
        verify(userRepository, never()).save(any(UserInfo.class));
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
        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    void deleteExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(777L).login("user").password("qwerty123!").role(UserRole.USER).build();
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .id(777L).login("user").password("qwerty123!").role(UserRole.USER).build();
        when(userRepository.findById(777L)).thenReturn(
                Optional.ofNullable(user)
        );

        //When
        Optional<UserInfoDTO> deletedUser = instance.delete(777);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isPresent()
                .get()
                .isEqualToComparingFieldByField(expected);
        verify(userRepository).delete(user);
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
        verify(userRepository, never()).deleteById(id);
        verify(userRepository, never()).delete(any(UserInfo.class));
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
        UserInfoDTO expected = UserInfoDTOBuilder.getInstance()
                .id(1L).login("awesomeUser").password("qwerty123!").role(UserRole.USER).build();
        when(userRepository.findAllByRole(UserRole.USER))
                .thenReturn(asList(
                        user,
                        mockUser)
                );

        //When
        List<UserInfoDTO> allUsers = instance.findAllByRole(UserRole.USER);

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(2)
                .usingFieldByFieldElementComparator()
                .contains(expected, mockUserDTO);
    }


    @Test
    void findByRoleAllAdmin() {
        //Given
        UserInfo admin = new UserInfoBuilder().id(1L).login("ADMIN").password("qwerty123!").role(UserRole.ADMIN).build();
        UserInfoDTO expectedUser = UserInfoDTOBuilder.getInstance()
                .id(1L).login("ADMIN").password("qwerty123!").role(UserRole.ADMIN).build();
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