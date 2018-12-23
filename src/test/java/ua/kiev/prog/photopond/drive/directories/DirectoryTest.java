package ua.kiev.prog.photopond.drive.directories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@ExtendWith(SpringExtension.class)
public class DirectoryTest {

    private static final String OWNER_NAME = "awesomeUser";
    private static final String ROOT_PATH = SEPARATOR + OWNER_NAME;

    private Directory directory;

    @BeforeEach
    public void setUp() {
        UserInfo owner = new UserInfoBuilder().id(123L).login(OWNER_NAME).build();
        directory = new DirectoryBuilder().owner(owner).build();
    }

    @Test
    public void nullPath() {
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(null)
        );
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

    @Test
    public void getOwnerFolderForNullOwner() {
        //Given
        directory = new Directory();

        //When
        assertThrows(IllegalStateException.class,
                () -> directory.getOwnerFolder()
        );
    }

    @Test
    public void getParentPathForNullPath() {
        //Given
        directory = new Directory();

        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.parentPath()
        );
    }

    @Test
    public void setNullAsOwner() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setOwner(null)
        );
    }

    @Test
    public void getDirectoryNamesWhenNullOwner() {
        //Given
        directory = new Directory();

        //When
        assertThrows(IllegalStateException.class,
                () -> directory.getDirectoryNames()
        );
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

    @Test
    public void setPathEndsOnSeparator() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(SEPARATOR + "someDirectory" + SEPARATOR)
        );
    }

    @Test
    public void setPathWithTwoSeparatorTogether() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(SEPARATOR + SEPARATOR)
        );
    }

    @Test
    public void setPathWithSeveralSeparatorTogether() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(SEPARATOR + SEPARATOR + SEPARATOR + SEPARATOR + "someNameDirectory")
        );
    }

    @Test
    public void setPathNotStartsWithSeparator() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath("somePath")
        );
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

    @Test
    public void buildPathFromNullName() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath(null, "first", "second")
        );
    }

    @Test
    public void buildPathWhenNamesContainNull() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath("first", null, "second")
        );
    }

    @Test
    public void buildPathFromEmptyName() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath("first", "", "second")
        );
    }

    @Test
    public void buildPathWhenRootDirectoryNotFirst() {
        //Given
        String expected = "/first/third";

        //When
        String result = buildPath("first", SEPARATOR, "third");

        //Then
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    public void buildPathWhenDirectoryContentsTwoSeparator() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath("first", SEPARATOR + SEPARATOR + "third")
        );
    }

    @Test
    public void retrieveParentPathFromNull() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> Directory.retrieveParentPath(null)
        );
    }

    @Test
    public void retrieveParentPathFromEmptyPath() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> Directory.retrieveParentPath("")
        );
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