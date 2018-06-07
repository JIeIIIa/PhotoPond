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
        //Given
        Long id = 777L;

        //When
        Directory directory = new DirectoryBuilder().id(id).build();

        //Then
        assertThat(directory.getId()).isEqualTo(id);
    }

    @Test
    public void owner() {
        //Given
        UserInfo user = new UserInfoBuilder()
                .id(123)
                .login("someUser")
                .role(UserRole.ADMIN)
                .build();

        //When
        Directory directory = new DirectoryBuilder().owner(user).build();

        //Then
        assertThat(directory.getOwner()).isEqualTo(user);
    }

    @Test
    public void path() {
        //Given
        String path = buildPath("some", "long", "long", "long", "path");

        //When
        Directory directory = new DirectoryBuilder().path(path).build();

        //Then
        assertThat(directory.getPath()).isEqualTo(path);
    }

    @Test
    public void defaultBuild() {
        //Given
        Directory expected = new Directory();
        expected.setId(Long.MIN_VALUE);
        expected.setOwner(new UserInfo());
        expected.setPath(Directory.SEPARATOR);

        //When
        Directory directory = new DirectoryBuilder().build();

        //Then
        assertThat(directory).isEqualTo(expected);
    }

    @Test
    public void from() {
        //Given
        UserInfo user = new UserInfoBuilder()
                .id(123)
                .login("someUser")
                .role(UserRole.ADMIN)
                .build();
        Directory directory = new Directory();
        directory.setId(777L);
        directory.setOwner(user);
        directory.setPath(buildPath("some","path"));

        //When
        Directory result = new DirectoryBuilder().from(directory).build();

        //Then
        assertThat(result.getId()).isEqualTo(777L);
        assertThat(result.getOwner()).isEqualTo(user);
        assertThat(result.getPath()).isEqualTo("/some/path");
    }
}