package ua.kiev.prog.photopond.drive.directories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DirectoryBuilderTest {
    @Test
    public void id() throws Exception {
        Long id = 777L;

        Directory directory = new DirectoryBuilder().id(id).build();

        assertThat(directory.getId()).isEqualTo(id);
    }

    @Test
    public void owner() throws Exception {
        UserInfo user = new UserInfoBuilder()
                .id(123)
                .login("someUser")
                .role(UserRole.ADMIN)
                .build();

        Directory directory = new DirectoryBuilder().owner(user).build();

        assertThat(directory.getOwner()).isEqualTo(user);
    }

    @Test
    public void path() throws Exception {
        String path = "/some/long/long/long/path";

        Directory directory = new DirectoryBuilder().path(path).build();

        assertThat(directory.getPath()).isEqualTo(path);
    }

    @Test
    public void defaultBuild() throws Exception {
        Directory directory = new DirectoryBuilder().build();

        Directory expected = new Directory();

        assertThat(directory).isEqualTo(expected);
    }

}