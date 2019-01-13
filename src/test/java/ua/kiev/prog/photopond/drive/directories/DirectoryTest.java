package ua.kiev.prog.photopond.drive.directories;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ua.kiev.prog.photopond.drive.directories.Directory.*;

@ExtendWith(SpringExtension.class)
class DirectoryTest {

    private static final String OWNER_NAME = "awesomeUser";
    private static final String ROOT_PATH = SEPARATOR + OWNER_NAME;

    private Directory directory;

    @BeforeEach
    void setUp() {
        UserInfo owner = new UserInfoBuilder().id(123L).login(OWNER_NAME).build();
        directory = new DirectoryBuilder().owner(owner).build();
    }

    @Test
    void nullPath() {
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(null)
        );
    }

    @Test
    void rootDirectory() {
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
    void rootDirectoryName() {
        //Given
        directory.setPath(SEPARATOR);

        //Then
        assertThat(directory.getName())
                .isEqualTo("");
    }

    @Test
    void firstLevelDirectoryName() {
        //Given
        String directoryName = "awesomeDirectory";

        //When
        directory.setPath(SEPARATOR + directoryName);

        //Then
        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    void moreThanOneLevelDirectoryName() {
        //Given
        String directoryName = "awesomeDirectory";

        //When
        directory.setPath(buildPath("first", "Second", directoryName));

        //Then
        assertThat(directory.getName())
                .isEqualTo(directoryName);
    }

    @Test
    void pathWithSubdirectories() {
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
    void getParentPath() {
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
    void getParentPathForFirstLevel() {
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
    void getOwnerFolderForNullOwner() {
        //Given
        directory = new Directory();

        //When
        assertThrows(IllegalStateException.class,
                () -> directory.getOwnerFolder()
        );
    }

    @Test
    void getParentPathForNullPath() {
        //Given
        directory = new Directory();

        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.parentPath()
        );
    }

    @Test
    void setNullAsOwner() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setOwner(null)
        );
    }

    @Test
    void getDirectoryNamesWhenNullOwner() {
        //Given
        directory = new Directory();

        //When
        assertThrows(IllegalStateException.class,
                () -> directory.getDirectoryNames()
        );
    }

    @Test
    void getDirectoryNamesWithoutSubdirectories() {
        //When
        directory.setPath(SEPARATOR);

        //Then
        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(1)
                .contains(directory.getOwner().getLogin());
    }

    @Test
    void getDirectoryNamesWithSubdirectories() {
        //When
        directory.setPath(buildPath("first", "second", "third", "etc"));

        //Then
        assertThat(directory.getDirectoryNames())
                .isNotNull()
                .hasSize(5)
                .containsExactly(directory.getOwner().getLogin(), "first", "second", "third", "etc");
    }

    @Test
    void setPathEndsOnSeparator() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(SEPARATOR + "someDirectory" + SEPARATOR)
        );
    }

    @Test
    void setPathWithTwoSeparatorTogether() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(SEPARATOR + SEPARATOR)
        );
    }

    @Test
    void setPathWithSeveralSeparatorTogether() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath(SEPARATOR + SEPARATOR + SEPARATOR + SEPARATOR + "someNameDirectory")
        );
    }

    @Test
    void setPathNotStartsWithSeparator() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> directory.setPath("somePath")
        );
    }

    @Test
    void buildPathFromOneDirectoryName() {
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
    void buildPathFromSeveralDirectoryName() {
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
    void buildPathFromPathAndName() {
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
    void buildPathFromPaths() {
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
    void buildPathFromFirstRootPathOnly() {
        //When
        String path = buildPath(SEPARATOR);

        //Then
        assertThat(path)
                .isNotNull()
                .isEqualTo(SEPARATOR);
    }

    @Test
    void buildPathFromFirstRootPath() {
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
    void buildPathFromNullName() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath(null, "first", "second")
        );
    }

    @Test
    void buildPathWhenNamesContainNull() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath("first", null, "second")
        );
    }

    @Test
    void buildPathFromEmptyName() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath("first", "", "second")
        );
    }

    @Test
    void buildPathWhenRootDirectoryNotFirst() {
        //Given
        String expected = "/first/third";

        //When
        String result = buildPath("first", SEPARATOR, "third");

        //Then
        assertThat(result)
                .isEqualTo(expected);
    }

    @Test
    void buildPathWhenDirectoryContentsTwoSeparator() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> buildPath("first", SEPARATOR + SEPARATOR + "third")
        );
    }

    static Stream<Arguments> appendToPathStream() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of("/first", null, "/first"),
                Arguments.of("/first", "", "/first"),
                Arguments.of("first", "", "first"),
                Arguments.of("/first", "second", "/first/second")
        );
    }

    @ParameterizedTest(name = "[{index}] = path={0} name={1}")
    @MethodSource(value = {"appendToPathStream"})
    void appendToPathTest(String path, String name, String expected) {
        //When
        String result = appendToPath(path, name);

        //Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void retrieveParentPathFromNull() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> Directory.retrieveParentPath(null)
        );
    }

    @Test
    void retrieveParentPathFromEmptyPath() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> Directory.retrieveParentPath("")
        );
    }

    @Test
    void retrieveParentPathFromRoot() {
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
    void retrieveParentPathFromFirstLevelDirectory() {
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
    void retrieveParentPathFromSubDirectory() {
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
    void nameForBreadcrumbRootDirectory() {
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
    void nameForBreadcrumbWithSubDirectories() {
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
    void isRootForRootDirectory() {
        //When
        boolean result = Directory.isRoot(SEPARATOR);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void isRootForSubDirectory() {
        //Given
        String path = buildPath("first", "second", "third");

        //When
        boolean result = Directory.isRoot(path);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void equals() {
        EqualsVerifier.forClass(Directory.class)
                .usingGetClass()
                .withIgnoredFields("id", "creationDate")
                .verify();
    }
}