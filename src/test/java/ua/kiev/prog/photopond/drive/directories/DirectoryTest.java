package ua.kiev.prog.photopond.drive.directories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryTest {

    private static final String OWNER_NAME = "awesomeUser";
    private static final String ROOT_PATH = Directory.SEPARATOR + OWNER_NAME;

    private Directory directory;

    @Before
    public void setUp() throws Exception {
        UserInfo owner = new UserInfoBuilder().id(123).login(OWNER_NAME).build();
        directory = new DirectoryBuilder().owner(owner).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPath() throws Exception {
        directory.setPath(null);
    }

    @Test
    public void rootDirectory() throws Exception {
        directory.setPath(Directory.SEPARATOR);

        String expectedName = Directory.SEPARATOR;

        assertThat(directory.getLevel())
                .isEqualTo(1);
        assertThat(directory.getPath())
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(expectedName);
        assertThat(directory.getParentPath())
                .isNotNull()
                .isEqualTo("");
        assertThat(directory.getOwnerFolder())
                .isNotNull()
                .isEqualTo(ROOT_PATH);
        assertThat(directory.getFullPath())
                .isNotNull()
                .isEqualTo(ROOT_PATH);
    }

    @Test
    public void rootDirectoryName() throws Exception {
        directory.setPath(Directory.SEPARATOR);

        assertThat(directory.getName())
                .isEqualTo("");
    }

    @Test
    public void firstLevelDirectoryName() throws Exception {
        String directoryName = "awesomeDirectory";
        directory.setPath(Directory.SEPARATOR + directoryName);

        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void moreThanOneLevelDirectoryName() throws Exception {
        String directoryName = "awesomeDirectory";
        directory.setPath(Directory.SEPARATOR + "first" + Directory.SEPARATOR + "Second" + Directory.SEPARATOR + directoryName);

        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void pathWithSubdirectories() throws Exception {
        String expectedParentName = new StringBuilder()
                .append(Directory.SEPARATOR).append("first")
                .append(Directory.SEPARATOR).append("second")
                .toString();
        String path = expectedParentName + Directory.SEPARATOR + "third";
        directory.setPath(path);


        assertThat(directory.getLevel()).isEqualTo(3);
        assertThat(directory.getPath())
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(path);
        assertThat(directory.getParentPath())
                .isNotNull()
                .isEqualTo(expectedParentName);
        assertThat(directory.getOwnerFolder())
                .isNotNull()
                .isEqualTo(ROOT_PATH);
        assertThat(directory.getFullPath())
                .isNotNull()
                .isEqualTo(ROOT_PATH + path);
    }

    @Test
    public void getParentPath() throws Exception {
        String expectedParentName = new StringBuilder()
                .append(Directory.SEPARATOR).append("first")
                .append(Directory.SEPARATOR).append("second")
                .toString();
        String path = expectedParentName + Directory.SEPARATOR + "third";
        directory.setPath(path);

        assertThat(directory.getParentPath())
                .isNotNull()
                .isEqualTo(expectedParentName);
    }

    @Test
    public void getParentPathForFirstLevel() throws Exception {
        String expectedParentPath = Directory.SEPARATOR;
        String path = Directory.SEPARATOR + "someDirectory";
        directory.setPath(path);

        assertThat(directory.getParentPath())
                .isNotNull()
                .isEqualTo(expectedParentPath);
    }

    @Test(expected = IllegalStateException.class)
    public void getRootForNullOwner() throws Exception {
        directory = new Directory();

        directory.getOwnerFolder();
    }

    @Test(expected = IllegalStateException.class)
    public void getParentPathForNullOwner() throws Exception {
        directory = new Directory();

        directory.getOwnerFolder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getParentPathForNullPath() throws Exception {
        directory.getParentPath();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNullAsOwner() throws Exception {
        directory.setOwner(null);
    }

    @Test
    public void parentDirectoryNamesWithoutSubdirectories() throws Exception {
        directory.setPath("/");

        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(1)
                .contains(directory.getOwner().getLogin());
    }

    @Test
    public void parentDirectoryNamesWithSubdirectories() throws Exception {
        directory.setPath("/first/second/third/etc");

        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(5)
                .containsExactly(directory.getOwner().getLogin(), "first", "second", "third", "etc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void pathEndsOnSeparator() throws Exception {
        directory.setPath(Directory.SEPARATOR + "someDirectory" + Directory.SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void pathWithSeveralSeparatorTogether() throws Exception {
        directory.setPath(Directory.SEPARATOR + Directory.SEPARATOR + Directory.SEPARATOR + Directory.SEPARATOR + "someNameDirectory");
    }


}