package ua.kiev.prog.photopond.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class UserInfoBuilderTest {
    @Test
    public void id() throws Exception {
        long id = 777;

        UserInfo userInfo = new UserInfoBuilder().id(id).build();

        assertThat(userInfo.getId()).isEqualTo(id);
    }

    @Test
    public void login() throws Exception {
        String login = "someUser";

        UserInfo userInfo = new UserInfoBuilder().login(login).build();

        assertThat(userInfo.getLogin()).isEqualTo(login);
    }

    @Test
    public void password() throws Exception {
        String password = "securedPassword";

        UserInfo userInfo = new UserInfoBuilder().password(password).build();

        assertThat(userInfo.getPassword()).isEqualTo(password);
    }

    @Test
    public void role() throws Exception {
        UserRole role = UserRole.ADMIN;

        UserInfo userInfo = new UserInfoBuilder().role(role).build();

        assertThat(userInfo.getRole()).isEqualTo(role);
    }

    @Test
    public void defaultBuild() throws Exception {
        UserInfo userInfo = new UserInfoBuilder().build();

        assertThat(userInfo.getId()).isEqualTo(0);
        assertThat(userInfo.getLogin())
                .isNotNull()
                .isEqualTo("");
        assertThat(userInfo.getPassword())
                .isNotNull()
                .isEqualTo("");
        assertThat(userInfo.getRole()).isEqualTo(UserRole.USER);
    }
}