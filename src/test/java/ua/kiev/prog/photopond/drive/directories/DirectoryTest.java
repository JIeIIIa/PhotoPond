package ua.kiev.prog.photopond.drive.directories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryTest {

    private static final String OWNER_NAME = "awesomeUser";
    private static final String ROOT_PATH = SEPARATOR + OWNER_NAME;

    private Directory directory;

    @Before
    public void setUp() {
        UserInfo owner = new UserInfoBuilder().id(123).login(OWNER_NAME).build();
        directory = new DirectoryBuilder().owner(owner).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPath() {
        directory.setPath(null);
    }

    @Test
    public void rootDirectory() {
        directory.setPath(SEPARATOR);

        String expectedName = SEPARATOR;

        assertThat(directory.getLevel())
                .isNotNull()
                .isEqualTo(0);
        assertThat(directory.getPath())
                .isNotNull()
                .isEqualTo(expectedName);
        assertThat(directory.parentPath())
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
    public void rootDirectoryName() {
        directory.setPath(SEPARATOR);

        assertThat(directory.getName())
                .isEqualTo("");
    }

    @Test
    public void firstLevelDirectoryName() {
        String directoryName = "awesomeDirectory";
        directory.setPath(SEPARATOR + directoryName);

        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void moreThanOneLevelDirectoryName() {
        String directoryName = "awesomeDirectory";
        directory.setPath(buildPath("first", "Second", directoryName));

        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void pathWithSubdirectories() {
        String expectedParentName = buildPath("first", "second");
        String path = buildPath(expectedParentName, "third");

        directory.setPath(path);

        assertThat(directory.getLevel()).isEqualTo(3);
        assertThat(directory.getPath())
                .isNotNull()
                .isEqualTo(path);
        assertThat(directory.parentPath())
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
    public void getParentPath() {
        String expectedParentName = buildPath("first", "second");
        String path = buildPath(expectedParentName, "third");
        directory.setPath(path);

        assertThat(directory.parentPath())
                .isNotNull()
                .isEqualTo(expectedParentName);
    }

    @Test
    public void getParentPathForFirstLevel() {
        String expectedParentPath = SEPARATOR;
        String path = SEPARATOR + "someDirectory";
        directory.setPath(path);

        assertThat(directory.parentPath())
                .isNotNull()
                .isEqualTo(expectedParentPath);
    }

    @Test(expected = IllegalStateException.class)
    public void getRootForNullOwner() {
        directory = new Directory();

        directory.getOwnerFolder();
    }

    @Test(expected = IllegalStateException.class)
    public void getParentPathForNullOwner() {
        directory = new Directory();

        directory.getOwnerFolder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getParentPathForNullPath() {
        directory = new Directory();
        directory.parentPath();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNullAsOwner() {
        directory.setOwner(null);
    }

    @Test
    public void parentDirectoryNamesWithoutSubdirectories() {
        directory.setPath(SEPARATOR);

        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(1)
                .contains(directory.getOwner().getLogin());
    }

    @Test
    public void parentDirectoryNamesWithSubdirectories() {
        directory.setPath(buildPath("first", "second", "third", "etc"));

        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(5)
                .containsExactly(directory.getOwner().getLogin(), "first", "second", "third", "etc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathEndsOnSeparator() {
        directory.setPath(SEPARATOR + "someDirectory" + SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathWithTwoSeparatorTogether() {
        directory.setPath(SEPARATOR + SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathWithSeveralSeparatorTogether() {
        directory.setPath(SEPARATOR + SEPARATOR + SEPARATOR + SEPARATOR + "someNameDirectory");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathNotStartsWithSeparator() {
        String path = "somePath";

        directory.setPath(path);
    }

    @Test
    public void buildPathFromOneDirectoryName() {
        String firstName = "first";
        String expectedPath = SEPARATOR + firstName;

        String path = buildPath(firstName);

        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromSeveralDirectoryName() {
        String firstName = "first";
        String[] names = {"second", "third"};
        String expectedPath = SEPARATOR + firstName
                + SEPARATOR + names[0]
                + SEPARATOR + names[1];

        String path = buildPath(firstName, names);

        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromPathAndName() {
        String firstName = SEPARATOR + "first" + SEPARATOR + "second";
        String[] names = {"third"};
        String expectedPath = SEPARATOR + "first"
                + SEPARATOR + "second"
                + SEPARATOR + "third";

        String path = buildPath(firstName, names);

        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromPaths() {
        String firstName = SEPARATOR + "first" + SEPARATOR + "second";
        String[] names = {SEPARATOR + "third"};
        String expectedPath = SEPARATOR + "first"
                + SEPARATOR + "second"
                + SEPARATOR + "third";

        String path = buildPath(firstName, names);

        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromFirstRootPath() {
        String firstName = SEPARATOR;
        String[] names = {SEPARATOR + "second", SEPARATOR + "third"};
        String expectedPath = SEPARATOR + "second"
                + SEPARATOR + "third";

        String path = buildPath(firstName, names);

        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathFromNullName() {
        buildPath("first", null, "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathFromEmptyName() {
        buildPath("first", "", "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenRootDirectoryNotFirst() {
        buildPath("first", SEPARATOR, "third");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenDirectoryContentsTwoSeparator() {
        buildPath("first", SEPARATOR + SEPARATOR + "third");
    }
}