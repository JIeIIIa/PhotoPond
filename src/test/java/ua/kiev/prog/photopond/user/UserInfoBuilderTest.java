package ua.kiev.prog.photopond.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class UserInfoBuilderTest {
    @Test
    public void id() {
        //Given
        long id = 777;

        //When
        UserInfo userInfo = new UserInfoBuilder().id(id).build();

        //Then
        assertThat(userInfo.getId()).isEqualTo(id);
    }

    @Test
    public void login() {
        //Given
        String login = "someUser";

        //When
        UserInfo userInfo = new UserInfoBuilder().login(login).build();

        //Then
        assertThat(userInfo.getLogin()).isEqualTo(login);
    }

    @Test
    public void password() {
        //Given
        String password = "securedPassword";

        //When
        UserInfo userInfo = new UserInfoBuilder().password(password).build();

        //Then
        assertThat(userInfo.getPassword()).isEqualTo(password);
    }

    @Test
    public void role() {
        //Given
        UserRole role = UserRole.ADMIN;

        //When
        UserInfo userInfo = new UserInfoBuilder().role(role).build();

        //Then
        assertThat(userInfo.getRole()).isEqualTo(role);
    }

    @Test
    public void avatar() {
        //Given
        byte[] avatar = {1, 2, 3, 4, 5, 6, 7};
        byte[] expected = {1, 2, 3, 4, 5, 6, 7};

        //When
        UserInfo userInfo = new UserInfoBuilder().avatar(avatar).build();

        //Then
        assertThat(userInfo.getAvatar())
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    public void defaultBuild() {
        //When
        UserInfo userInfo = new UserInfoBuilder().build();

        //Then
        assertThat(userInfo.getId()).isEqualTo(Long.MIN_VALUE);
        assertThat(userInfo.getLogin())
                .isNotNull()
                .isEqualTo("");
        assertThat(userInfo.getPassword())
                .isNotNull()
                .isEqualTo("");
        assertThat(userInfo.getRole()).isEqualTo(UserRole.USER);
    }
}