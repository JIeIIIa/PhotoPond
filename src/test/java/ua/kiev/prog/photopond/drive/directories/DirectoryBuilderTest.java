package ua.kiev.prog.photopond.drive.directories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@RunWith(JUnit4.class)
public class DirectoryBuilderTest {
    @Test
    public void id() {
        Long id = 777L;

        Directory directory = new DirectoryBuilder().id(id).build();

        assertThat(directory.getId()).isEqualTo(id);
    }

    @Test
    public void owner() {
        UserInfo user = new UserInfoBuilder()
                .id(123)
                .login("someUser")
                .role(UserRole.ADMIN)
                .build();

        Directory directory = new DirectoryBuilder().owner(user).build();

        assertThat(directory.getOwner()).isEqualTo(user);
    }

    @Test
    public void path() {
        String path = buildPath("some", "long", "long", "long", "path");

        Directory directory = new DirectoryBuilder().path(path).build();

        assertThat(directory.getPath()).isEqualTo(path);
    }

    @Test
    public void defaultBuild() {
        Directory directory = new DirectoryBuilder().build();

        Directory expected = new Directory();
        expected.setId(Long.MIN_VALUE);
        expected.setOwner(new UserInfo());
        expected.setPath(Directory.SEPARATOR);

        assertThat(directory).isEqualTo(expected);
    }

    @Test
    public void from() {
        UserInfo user = new UserInfoBuilder()
                .id(123)
                .login("someUser")
                .role(UserRole.ADMIN)
                .build();
        Directory directory = new Directory();
        directory.setId(777L);
        directory.setOwner(user);
        directory.setPath(buildPath("some","path"));

        Directory result = new DirectoryBuilder().from(directory).build();

        assertThat(result.getId()).isEqualTo(777L);
        assertThat(result.getOwner()).isEqualTo(user);
        assertThat(result.getPath()).isEqualTo("/some/path");
    }
}