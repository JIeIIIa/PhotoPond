package ua.kiev.prog.photopond.user;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "testDB", "unitTest"})
@EnableAutoConfiguration
@DataJpaTest
@ContextConfiguration(classes = {UserInfoServiceJpaImplITConfiguration.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:datasets/users_dataset.xml")
public class UserInfoServiceJpaImplIT {
    @Autowired
    private UserInfoJpaRepository userInfoJpaRepository;

    @Autowired
    private UserInfoServiceJpaImpl userInfoServiceJpaImpl;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void existsUserByLoginSuccess() {
        //When
        boolean isExistsUser = userInfoServiceJpaImpl.existsByLogin("someUser");

        //Then
        assertThat(isExistsUser).isTrue();
    }

    @Test
    public void existsUserByLoginFailure() {
        //When
        boolean isExistsUser = userInfoServiceJpaImpl.existsByLogin("unknownUser");

        //Then
        assertThat(isExistsUser).isFalse();
    }

    @Test
    public void addUserSuccess() {
        //Given
        String password = "strongPassword";
        UserInfoDTO userDTO = UserInfoDTOBuilder.getInstance()
                .login("newUser").password(password).role(UserRole.USER)
                .build();
        UserInfo expectedUser = new UserInfoBuilder().login("newUser").password(password).role(UserRole.USER).build();

        //When
        userInfoServiceJpaImpl.addUser(userDTO);

        //Then
        List<UserInfo> allUsers = userInfoJpaRepository.findAll();
        Optional<UserInfo> createdUser = userInfoJpaRepository.findByLogin("newUser");

        assertThat(allUsers)
                .hasSize(4)
                .usingElementComparatorIgnoringFields("id", "password")
                .contains(expectedUser);
        assertThat(createdUser)
                .isPresent()
                .hasValueSatisfying(u -> passwordEncoder.matches(expectedUser.getPassword(), u.getPassword()));
    }


    @Test
    public void addNullAsUser() {
        //When
        userInfoServiceJpaImpl.addUser(null);

        //Then
        List<UserInfo> allUsers = userInfoJpaRepository.findAll();

        assertThat(allUsers)
                .hasSize(3);
    }

    @Test
    public void findByLoginExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder()
                .id(777L)
                .login("someUser")
                .password("password")
                .role(UserRole.USER)
                .build();

        //When
        UserInfo foundUser = userInfoServiceJpaImpl.findUserByLogin(user.getLogin()).orElseThrow(IllegalStateException::new);

        //Then
        assertThat(foundUser)
                .isNotNull()
                .isEqualToIgnoringGivenFields(user, "password")
                .matches(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()));
    }

    @Test
    public void findByLoginNotExistsUser() {
        //When
        Optional<UserInfo> foundUser = userInfoServiceJpaImpl.findUserByLogin("unknownUser");

        //Then
        assertThat(foundUser)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void findByLoginWhenLoginIsNull() {
        //When
        Optional<UserInfo> user = userInfoServiceJpaImpl.findUserByLogin(null);

        //Then
        assertThat(user)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void findByIdExistUser() {
        //Given
        UserInfo expectedUser = new UserInfoBuilder()
                .id(777L)
                .login("someUser")
                .password("password")
                .role(UserRole.USER)
                .build();

        //When
        Optional<UserInfo> foundUser = userInfoServiceJpaImpl.findById(777);

        //Then
        assertThat(foundUser)
                .isNotNull()
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(expectedUser, "password")
                .matches(u -> passwordEncoder.matches(expectedUser.getPassword(), u.getPassword()));
    }

    @Test
    public void findByIdNotExistsUser() {
        //When
        Optional<UserInfo> foundUser = userInfoServiceJpaImpl.findById(123);

        //Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    public void findAllUsers() {
        //Given
        UserInfo[] allUsers = {
                new UserInfoBuilder().id(777L).login("someUser").password("password").role(UserRole.USER).build(),
                new UserInfoBuilder().id(1L).login("Administrator").password("qwerty123!").role(UserRole.ADMIN).build(),
                new UserInfoBuilder().id(2L).login("disabledUser").password("password").role(UserRole.DEACTIVATED).build(),
        };

        //When
        List<UserInfo> foundUsers = userInfoServiceJpaImpl.findAllUsers();

        //Then
        assertThat(foundUsers)
                .isNotNull()
                .hasSameSizeAs(allUsers)
                .usingElementComparatorIgnoringFields("password")
                .containsExactlyInAnyOrder(allUsers);
    }

    @Test
    public void updatePasswordSuccess() {
        //Given
        String login = "someUser";
        String oldPassword = "password";
        String newPassword = "qwerty";

        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .login(login)
                .oldPassword(oldPassword)
                .password(newPassword)
                .passwordConfirmation(newPassword)
                .build();

        UserInfo expectedUser = new UserInfoBuilder()
                .id(777L)
                .login(login)
                .password(passwordEncoder.encode(newPassword))
                .role(UserRole.USER)
                .build();
        //When
        boolean result = userInfoServiceJpaImpl.setNewPassword(userInfoDTO);

        //Then
        Optional<UserInfo> afterUpdate = userInfoServiceJpaImpl.findById(expectedUser.getId());

        assertThat(result).isTrue();
        assertThat(afterUpdate)
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(expectedUser, "password")
                .matches(u -> passwordEncoder.matches(newPassword, u.getPassword()));
    }

    @Test
    public void updateWithPasswordNotNullSuccess() {
        //Given
        String newPassword = "newPassword";
        UserInfo newInformation = new UserInfoBuilder()
                .id(777L)
                .login("user")
                .password(newPassword)
                .role(UserRole.ADMIN).build();
        UserInfo expected = new UserInfo().copyFrom(newInformation);
        expected.setPassword(passwordEncoder.encode(newInformation.getPassword()));

        //When
        Optional<UserInfo> result = userInfoServiceJpaImpl.update(newInformation);

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
        UserInfo expectedUser = new UserInfo().copyFrom(newInformation);
        expectedUser.setPassword("password");

        //When
        Optional<UserInfo> result = userInfoServiceJpaImpl.update(newInformation);

        //Then
        assertThat(result)
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(expectedUser, "password")
                .matches(u -> passwordEncoder.matches(expectedUser.getPassword(), u.getPassword()));
    }


    @Test
    public void updateWhenExistsSameLogin() {
        //Given
        UserInfo user = userInfoJpaRepository.findById(777L).orElseThrow(IllegalStateException::new);
        UserInfo newInformation = new UserInfoBuilder()
                .id(777L)
                .login("Administrator")
                .password("newPassword")
                .role(UserRole.ADMIN).build();

        //When
        Optional<UserInfo> userAfterUpdate = userInfoServiceJpaImpl.update(newInformation);

        //Then
        Optional<UserInfo> userCheck = userInfoJpaRepository.findById(777L);
        assertThat(userAfterUpdate).isNotPresent();
        assertThat(userCheck)
                .isPresent().get()
                .isEqualToComparingFieldByField(user);
    }

    @Test
    public void updateWithWrongId() {
        //Given
        UserInfo newInformation = new UserInfoBuilder()
                .id(101010L)
                .login("newUser")
                .password("password")
                .build();

        //When
        Optional<UserInfo> userAfterUpdate = userInfoServiceJpaImpl.update(newInformation);

        //Then
        List<UserInfo> allUsers = userInfoJpaRepository.findAll();

        assertThat(userAfterUpdate).isNotPresent();
        assertThat(allUsers)
                .hasSize(3)
                .doesNotContain(newInformation);

    }

    @Test
    public void deleteExistsUser() {
        //Given
        UserInfo user = new UserInfoBuilder().id(777L).login("someUser").password("password").role(UserRole.USER).build();

        //When
        Optional<UserInfo> deletedUser = userInfoServiceJpaImpl.delete(777);

        //Then
        assertThat(userInfoJpaRepository.findById(777L)).isNotPresent();
        assertThat(deletedUser)
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(user, "password")
                .matches(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()));
    }

    @Test
    public void deleteWithFailureId() {
        //When
        Optional<UserInfo> deletedUser = userInfoServiceJpaImpl.delete(101010);

        //Then
        assertThat(deletedUser)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void existsUserWithAdminRole() {
        //When
        boolean isExists = userInfoServiceJpaImpl.existsWithAdminRole();

        //Then
        assertThat(isExists).isTrue();
    }

    @Test
    public void notExistsUserWithAdminRole() {
        //Given
        userInfoJpaRepository.findAllByRole(UserRole.ADMIN)
                .forEach(userInfoJpaRepository::delete);

        //When
        boolean isExists = userInfoServiceJpaImpl.existsWithAdminRole();

        //Then
        assertThat(isExists).isFalse();
    }

    @Test
    public void findByRoleAllUsers() {
        //Given
        UserInfo user = new UserInfoBuilder().id(777L).login("someUser").password("password").role(UserRole.USER).build();

        //When
        List<UserInfo> allUsers = userInfoServiceJpaImpl.findAllByRole(UserRole.USER);

        //Then
        assertThat(allUsers)
                .isNotNull()
                .hasSize(1)
                .first()
                .isEqualToIgnoringGivenFields(user, "password")
                .matches(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()));
    }

    @Test
    public void findByRoleAllAdmin() {
        //Given
        UserInfo admin = new UserInfoBuilder().id(1L).login("Administrator").password("qwerty123!").role(UserRole.ADMIN).build();

        //When
        List<UserInfo> allAdmin = userInfoServiceJpaImpl.findAllByRole(UserRole.ADMIN);

        //Then
        assertThat(allAdmin)
                .isNotNull()
                .hasSize(1)
                .first()
                .isEqualToIgnoringGivenFields(admin, "password")
                .matches(u -> passwordEncoder.matches(admin.getPassword(), u.getPassword()));
    }
}
