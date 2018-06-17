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
        //Given
        String expectedName = SEPARATOR;

        //When
        directory.setPath(SEPARATOR);

        //Then
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
        //Given
        directory.setPath(SEPARATOR);

        //Then
        assertThat(directory.getName())
                .isEqualTo("");
    }

    @Test
    public void firstLevelDirectoryName() {
        //Given
        String directoryName = "awesomeDirectory";

        //When
        directory.setPath(SEPARATOR + directoryName);

        //Then
        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void moreThanOneLevelDirectoryName() {
        //Given
        String directoryName = "awesomeDirectory";

        //When
        directory.setPath(buildPath("first", "Second", directoryName));

        //Then
        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    public void pathWithSubdirectories() {
        //Given
        String expectedParentName = buildPath("first", "second");
        String path = buildPath(expectedParentName, "third");

        //When
        directory.setPath(path);

        //Then
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
        //Given
        String expectedParentName = buildPath("first", "second");
        String path = buildPath(expectedParentName, "third");

        //When
        directory.setPath(path);

        //Then
        assertThat(directory.parentPath())
                .isNotNull()
                .isEqualTo(expectedParentName);
    }

    @Test
    public void getParentPathForFirstLevel() {
        //Given
        String expectedParentPath = SEPARATOR;
        String path = SEPARATOR + "someDirectory";

        //When
        directory.setPath(path);

        //Then
        assertThat(directory.parentPath())
                .isNotNull()
                .isEqualTo(expectedParentPath);
    }

    @Test(expected = IllegalStateException.class)
    public void getOwnerFolderForNullOwner() {
        //Given
        directory = new Directory();

        //When
        directory.getOwnerFolder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getParentPathForNullPath() {
        //Given
        directory = new Directory();

        //When
        directory.parentPath();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNullAsOwner() {
        //When
        directory.setOwner(null);
    }

    @Test(expected = IllegalStateException.class)
    public void getDirectoryNamesWhenNullOwner() {
        //Given
        directory = new Directory();

        //When
        directory.getDirectoryNames();
    }

    @Test
    public void getDirectoryNamesWithoutSubdirectories() {
        //When
        directory.setPath(SEPARATOR);

        //Then
        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(1)
                .contains(directory.getOwner().getLogin());
    }

    @Test
    public void getDirectoryNamesWithSubdirectories() {
        //When
        directory.setPath(buildPath("first", "second", "third", "etc"));

        //Then
        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(5)
                .containsExactly(directory.getOwner().getLogin(), "first", "second", "third", "etc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathEndsOnSeparator() {
        //When
        directory.setPath(SEPARATOR + "someDirectory" + SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathWithTwoSeparatorTogether() {
        //When
        directory.setPath(SEPARATOR + SEPARATOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathWithSeveralSeparatorTogether() {
        //When
        directory.setPath(SEPARATOR + SEPARATOR + SEPARATOR + SEPARATOR + "someNameDirectory");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPathNotStartsWithSeparator() {
        //When
        directory.setPath("somePath");
    }

    @Test
    public void buildPathFromOneDirectoryName() {
        //Given
        String firstName = "first";
        String expectedPath = SEPARATOR + firstName;

        //When
        String path = buildPath(firstName);

        //Then
        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromSeveralDirectoryName() {
        //Given
        String firstName = "first";
        String[] names = {"second", "third"};
        String expectedPath = SEPARATOR + firstName
                + SEPARATOR + names[0]
                + SEPARATOR + names[1];

        //When
        String path = buildPath(firstName, names);

        //Then
        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromPathAndName() {
        //Given
        String firstName = SEPARATOR + "first" + SEPARATOR + "second";
        String[] names = {"third"};
        String expectedPath = SEPARATOR + "first"
                + SEPARATOR + "second"
                + SEPARATOR + "third";

        //When
        String path = buildPath(firstName, names);

        //Then
        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromPaths() {
        //Given
        String firstName = SEPARATOR + "first" + SEPARATOR + "second";
        String[] names = {SEPARATOR + "third"};
        String expectedPath = SEPARATOR + "first"
                + SEPARATOR + "second"
                + SEPARATOR + "third";

        //When
        String path = buildPath(firstName, names);

        //Then
        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test
    public void buildPathFromFirstRootPath() {
        //Given
        String firstName = SEPARATOR;
        String[] names = {SEPARATOR + "second", SEPARATOR + "third"};
        String expectedPath = SEPARATOR + "second"
                + SEPARATOR + "third";

        //When
        String path = buildPath(firstName, names);

        //Then
        assertThat(path)
                .isNotNull()
                .isEqualTo(expectedPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathFromNullName() {
        //When
        buildPath(null, "first", "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenNamesContainNull() {
        //When
        buildPath("first", null, "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathFromEmptyName() {
        //When
        buildPath("first", "", "second");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenRootDirectoryNotFirst() {
        //When
        buildPath("first", SEPARATOR, "third");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildPathWhenDirectoryContentsTwoSeparator() {
        //When
        buildPath("first", SEPARATOR + SEPARATOR + "third");
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveParentPathFromNull() {
        //When
        Directory.retrieveParentPath(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveParentPathFromEmptyPath() {
        //When
        Directory.retrieveParentPath("");
    }

    @Test
    public void retrieveParentPathFromRoot() {
        //Given
        String expected = "";

        //When
        String result = Directory.retrieveParentPath(SEPARATOR);

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    public void retrieveParentPathFromFirstLevelDirectory() {
        //Given
        String path = buildPath("first");

        //When
        String result = Directory.retrieveParentPath(path);

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(SEPARATOR);
    }

    @Test
    public void retrieveParentPathFromSubDirectory() {
        //Given
        String path = buildPath("first", "second", "third");
        String expected = buildPath("first", "second");

        //When
        String result = Directory.retrieveParentPath(path);

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    public void nameForBreadcrumbRootDirectory() {
        //Given
        directory.setPath(SEPARATOR);

        //When
        String result = directory.nameForBreadcrumb();

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo("..");
    }


    @Test
    public void nameForBreadcrumbWithSubDirectories() {
        //Given
        String expected = "expectedName";
        directory.setPath(SEPARATOR + expected);

        //When
        String result = directory.nameForBreadcrumb();

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    public void isRootForRootDirectory() {
        //When
        boolean result = Directory.isRoot(SEPARATOR);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    public void isRootForSubDirectory() {
        //Given
        String path = buildPath("first", "second", "third");

        //When
        boolean result = Directory.isRoot(path);

        //Then
        assertThat(result).isFalse();
    }
}