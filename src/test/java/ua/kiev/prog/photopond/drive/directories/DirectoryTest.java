package ua.kiev.prog.photopond.drive.directories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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
        directory.setPath(SEPARATOR);

        String expectedName = SEPARATOR;

        assertThat(directory.getLevel())
                .isEqualTo(0);
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
        directory.setPath(SEPARATOR);

        assertThat(directory.getName())
                .isEqualTo("");
    }

    @Test
    public void firstLevelDirectoryName() throws Exception {
        String directoryName = "awesomeDirectory";
        directory.setPath(SEPARATOR + directoryName);

        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void moreThanOneLevelDirectoryName() throws Exception {
        String directoryName = "awesomeDirectory";
        directory.setPath(buildPath("first", "Second", directoryName));

        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void pathWithSubdirectories() throws Exception {
        String expectedParentName = buildPath("first", "second");
        String path = buildPath(expectedParentName, "third");
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
        String expectedParentName = buildPath("first", "second");
        String path = buildPath(expectedParentName, "third");
        directory.setPath(path);

        assertThat(directory.getParentPath())
                .isNotNull()
                .isEqualTo(expectedParentName);
    }

    @Test
    public void getParentPathForFirstLevel() throws Exception {
        String expectedParentPath = SEPARATOR;
        String path = SEPARATOR + "someDirectory";
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
        directory.setPath(SEPARATOR);

        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(1)
                .contains(directory.getOwner().getLogin());
    }

    @Test
    public void parentDirectoryNamesWithSubdirectories() throws Exception {
        directory.setPath(buildPath("first", "second", "third", "etc"));

        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(5)
                .containsExactly(directory.getOwner().getLogin(), "first", "second", "third", "etc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathEndsOnSeparator() throws Exception {
        directory.setPath(SEPARATOR + "someDirectory" + SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathWithSeveralSeparatorTogether() throws Exception {
        directory.setPath(SEPARATOR + SEPARATOR + SEPARATOR + SEPARATOR + "someNameDirectory");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathNotStartsWithSeparator() throws Exception {
        String path = "somePath";

        directory.setPath(path);
    }

    @Test
    public void buildPathFromOneDirectoryName() throws Exception {
        String firstName = "first";
        String expectedPath = SEPARATOR + firstName;

        String path = buildPath(firstName);

        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromSeveralDirectoryName() throws Exception {
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
    public void buildPathFromPathAndName() throws Exception {
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
    public void buildPathFromPaths() throws Exception {
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
    public void buildPathFromFirstRootPath() throws Exception {
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
    public void buildPathFromNullName() throws Exception {
        buildPath("first", null, "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathFromEmptyName() throws Exception {
        buildPath("first", "", "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenRootDirectoryNotFirst() throws Exception {
        buildPath("first", SEPARATOR, "third");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenDirectoryContentsTwoSeparator() throws Exception {
        buildPath("first", SEPARATOR + SEPARATOR + "third");
    }


}