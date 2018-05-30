package ua.kiev.prog.photopond.user;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "testDB", "unitTest"})
@DataJpaTest
@ContextConfiguration(classes = {UserInfoServiceJpaServiceImplITConfiguration.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:datasets/users_dataset.xml")
public class UserInfoServiceJpaServiceImplIT {
    @Autowired
    private UserInfoJpaRepository userInfoJpaRepository;

    @Autowired
    private UserInfoServiceJpaImpl userInfoServiceJpaImpl;

    @Test
    public void successExistsUserByLogin() {
        boolean isExistsUser = userInfoServiceJpaImpl.existByLogin("someUser");

        assertThat(isExistsUser).isTrue();
    }

    @Test
    public void failureExistsUserByLogin() {
        boolean isExistsUser = userInfoServiceJpaImpl.existByLogin("unknownUser");

        assertThat(isExistsUser).isFalse();
    }

    @Test
    public void successGetUserById() {
        UserInfo user = new UserInfoBuilder()
                .id(777)
                .login("someUser")
                .password("password")
                .role(UserRole.USER)
                .build();

        Optional<UserInfo> foundUser = userInfoServiceJpaImpl.getUserById(777);

        assertThat(foundUser)
                .isNotNull()
                .isPresent()
                .usingFieldByFieldValueComparator()
                .hasValue(user);
    }

    @Test
    public void failureGetUserById() {
        Optional<UserInfo> foundUser = userInfoServiceJpaImpl.getUserById(123);

        assertThat(foundUser.isPresent()).isFalse();
    }


    @Test
    public void getAllUsers() {
        List<UserInfo> allUsersFromRepository = userInfoJpaRepository.findAll();
        int size = allUsersFromRepository.size();
        UserInfo[] allUsers = allUsersFromRepository.toArray(new UserInfo[size]);

        List<UserInfo> foundUsers = userInfoServiceJpaImpl.getAllUsers();

        assertThat(foundUsers)
                .isNotNull()
                .hasSize(3)
                .contains(allUsers);
    }

    @Test
    public void successGetUserByLogin() {
        UserInfo user = new UserInfoBuilder()
                .id(777)
                .login("someUser")
                .password("password")
                .role(UserRole.USER)
                .build();

        UserInfo foundUser = userInfoServiceJpaImpl.getUserByLogin(user.getLogin()).get();

        assertThat(foundUser)
                .isNotNull()
                .isEqualToComparingFieldByField(user);
    }

    @Test
    public void failureGetUserByLogin() {
        Optional<UserInfo> foundUser = userInfoServiceJpaImpl.getUserByLogin("unknownUser");

        assertThat(foundUser)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void addUser() {
        UserInfo user = new UserInfoBuilder().login("newUser").password("strongPassword").role(UserRole.USER).build();

        userInfoServiceJpaImpl.addUser(user);
        user.setId(userInfoJpaRepository.findByLogin("newUser").get().getId());
        List<UserInfo> allUsers = userInfoJpaRepository.findAll();

        assertThat(allUsers)
                .hasSize(4)
                .contains(user);
    }

    @Test
    public void addNullAsUser() {
        userInfoServiceJpaImpl.addUser(null);
        List<UserInfo> allUsers = userInfoJpaRepository.findAll();

        assertThat(allUsers)
                .hasSize(3);
    }


    @Test
    public void update() {
        UserInfo updatedUser = new UserInfoBuilder()
                .id(777)
                .login("someUser123")
                .password("qwerty")
                .role(UserRole.ADMIN)
                .build();

        UserInfo afterUpdate = userInfoServiceJpaImpl.update(updatedUser);
        UserInfo userInDB = userInfoJpaRepository.findById(777L).get();

        assertThat(afterUpdate).isEqualToComparingFieldByField(updatedUser);
        assertThat(userInDB)
                .isNotNull()
                .isEqualToComparingFieldByField(updatedUser);
    }

    @Test
    public void updateToExistsLogin() {
        UserInfo user = userInfoJpaRepository.findById(777L).get();
        UserInfo newInformation = new UserInfoBuilder()
                .id(777)
                .login("Administrator")
                .password("newPassword")
                .role(UserRole.ADMIN).build();

        UserInfo userAfterUpdate = userInfoServiceJpaImpl.update(newInformation);
        UserInfo userCheck = userInfoJpaRepository.findById(777L).get();

        assertThat(userAfterUpdate).isNull();
        assertThat(userCheck)
                .isNotNull()
                .isEqualToComparingFieldByField(user);
    }

    @Test
    public void updateWithFailureId() {
        UserInfo newInformation = new UserInfoBuilder()
                .id(101010)
                .login("newUser")
                .password("password")
                .build();

        UserInfo userAfterUpdate = userInfoServiceJpaImpl.update(newInformation);
        List<UserInfo> allUsers = userInfoJpaRepository.findAll();

        assertThat(userAfterUpdate).isNull();
        assertThat(allUsers)
                .hasSize(3)
                .doesNotContain(newInformation);

    }

    @Test
    public void deleteExistsUser() {
        UserInfo user = userInfoJpaRepository.findById(777L).get();

        UserInfo deletedUser = userInfoServiceJpaImpl.delete(777);
        Optional<UserInfo> afterDeleteUser = userInfoJpaRepository.findById(777L);

        assertThat(deletedUser)
                .isNotNull()
                .isEqualToComparingFieldByField(user);
        assertThat(afterDeleteUser.isPresent()).isFalse();
    }

    @Test
    public void deleteWithFailureId() {
        UserInfo user = userInfoServiceJpaImpl.delete(101010);

        assertThat(user).isNull();
    }
}
