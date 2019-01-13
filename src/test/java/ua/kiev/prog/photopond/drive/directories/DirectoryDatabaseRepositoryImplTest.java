package ua.kiev.prog.photopond.drive.directories;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.drive.exception.DirectoryModificationException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@ExtendWith(SpringExtension.class)
@TestPropertySource({"classpath:application.properties"})
@ActiveProfiles({"test", DATABASE_STORAGE})
class DirectoryDatabaseRepositoryImplTest {

    @MockBean
    private DirectoryJpaRepository directoryJpaRepository;

    private DirectoryDatabaseRepositoryImpl instance;

    private UserInfo user;
    private Directory root;
    private Directory directory;


    @BeforeEach
    void setUp() {
        instance = new DirectoryDatabaseRepositoryImpl(directoryJpaRepository);

        user = new UserInfoBuilder().login("awesomeUser").role(UserRole.USER).build();
        root = createDirectory(1L, "/", user);
        directory = createDirectory(7L, "/first", user);
    }

    private List<Directory> createSubdirectoryList(Directory directory, String... subDirectoryNames) {
        AtomicLong id = new AtomicLong(1001L);

        return Arrays.stream(subDirectoryNames)
                .map(name -> buildPath(directory.getPath(), name))
                .map(p -> createDirectory(id.getAndIncrement(), p, user))
                .collect(Collectors.toList());
    }

    private Directory createDirectory(Long id, String path, UserInfo owner) {
        return new DirectoryBuilder()
                .id(id)
                .owner(owner)
                .path(path)
                .build();
    }

    private Directory createDirectory(Long id, String path) {
        UserInfo anotherOwner = new UserInfo("hiddenUser", "qwerty123");
        return createDirectory(id, path, anotherOwner);
    }

    @Test
    void saveNullDirectory() {
        //When
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> instance.save(null)
        );

    }

    @Test
    void saveWhenDirectoryNotExists() {
        //Given
        when(directoryJpaRepository.save(any(Directory.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        instance.save(directory);

        //Then
        verify(directoryJpaRepository).save(directory);
    }

    @Test
    void saveWhenDirectoryExists() {
        //Given
        when(directoryJpaRepository.save(any(Directory.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        instance.save(directory);

        //Then
        verify(directoryJpaRepository).save(directory);
    }

    @Test
    void saveWhenParentDirectoryNotExists() {
        //Given
        String parentPath = buildPath("First");
        directory.setPath(buildPath(parentPath, "second"));
        when(directoryJpaRepository.findByOwnerAndPath(user, parentPath)).thenReturn(emptyList());

        //When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.save(directory)
        );

        //Then
        verify(directoryJpaRepository).findByOwnerAndPath(user, directory.parentPath());
    }

    @Test
    void deleteNullDirectory() {
        //When
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> instance.delete(null)
        );
    }

    @Test
    void deleteEmptyDirectory() {
        //Given
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(new LinkedList<>());

        //When
        instance.delete(directory);

        //Then
        verify(directoryJpaRepository).deleteAll(singletonList(directory));
    }

    @Test
    void deleteWithSubdirectories() {
        //Given
        List<Directory> subdirectories = createSubdirectoryList(directory, "one", "two", "three");
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(new LinkedList<>(subdirectories));

        //When
        instance.delete(directory);

        //Then
        List<Directory> deleted = new LinkedList<>(subdirectories);
        deleted.add(directory);
        verify(directoryJpaRepository).deleteAll(deleted);
    }

    @Test
    void deleteWithJpaRepositoryException() {
        //Given
        doThrow(QueryTimeoutException.class).when(directoryJpaRepository).deleteAll(singletonList(directory));

        //When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.delete(directory)
        );

        //Then
        verify(directoryJpaRepository).deleteAll(singletonList(directory));
    }

    @Test
    void renameTargetEqualsSource() throws DirectoryModificationException {
        //Given
        Directory expected = new DirectoryBuilder().from(directory).build();

        //When
        instance.rename(directory, directory.getPath());

        //Then
        verify(directoryJpaRepository, never()).save(any(Directory.class));
        verify(directoryJpaRepository, never()).findByOwnerAndPathStartingWith(any(UserInfo.class), any(String.class));
        assertThat(directory).isEqualTo(expected);
    }

    @Test
    void renameNotExistsDirectory() {
        // When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.rename(directory, "someName")
        );
    }

    @Test
    void renameSuccess() {
        //Given
        long id = 767L;
        String sourceName = "source";
        String[] sourceNames = {
                buildPath(directory.getPath(), sourceName),
                buildPath(directory.getPath(), sourceName, "smthng"),
                buildPath(directory.getPath(), sourceName, "another"),
                buildPath(directory.getPath(), sourceName, "another", "deep"),

        };
        String targetName = "target";
        String[] targetNames = {
                buildPath(directory.getPath(), targetName),
                buildPath(directory.getPath(), targetName, "smthng"),
                buildPath(directory.getPath(), targetName, "another"),
                buildPath(directory.getPath(), targetName, "another", "deep"),
        };

        List<Directory> directories = new LinkedList<>();
        for (String name : sourceNames) {
            directories.add(createDirectory(id, name, user));
        }
        Directory directoryInSameLevel = createDirectory(767L, sourceNames[0] + "TAIL", user);

        when(directoryJpaRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(user, sourceNames[0] + SEPARATOR))
                .thenReturn(asList(directories.get(1), directories.get(2), directories.get(3)));

        //When
        instance.rename(directories.get(0), targetNames[0]);

        //Then
        verify(directoryJpaRepository).findByOwnerAndPathStartingWith(directory.getOwner(), sourceNames[0] + SEPARATOR);

        assertThat(targetNames).hasSameSizeAs(sourceNames);
        assertThat(targetNames).hasSameSizeAs(directories);
        for (int i = 0; i < targetNames.length; i++) {
            assertThat(directories.get(i).getPath()).isEqualTo(targetNames[i]);
        }
    }

    @Test
    void renameWhenTargetDirectoryAlreadyExistsOnDisk() {
        //Given
        String newName = "anotherDir";
        String targetPath = buildPath(directory.parentPath(), newName);
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.parentPath()))
                .thenReturn(singletonList(root));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(emptyList());

        //When
        instance.rename(directory, targetPath);

        //Then
        assertThat(directory.getPath()).isEqualTo(targetPath);

    }

    @Test
    void renameWhenTargetDirectoryAlreadyExistsInDataBase() {
        //Given
        String newName = "anotherDir";
        String targetPath = buildPath(directory.parentPath(), newName);
        Directory targetDirectory = createDirectory(121L, targetPath, user);

        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.parentPath()))
                .thenReturn(singletonList(root));

        //When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.rename(this.directory, targetPath)
        );
    }

    @Test
    void moveNullDirectory() {
        //When
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> instance.move(null, directory)
        );
    }

    @Test
    void moveToNullDirectory() {
        //When
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> instance.move(directory, null)
        );

    }

    @Test
    void moveTargetDirectoryEqualsCurrentDirectory() {
        //Given
        Directory expectedDirectory = new DirectoryBuilder().from(directory).build();

        //When
        instance.move(directory, directory);

        //Then
        assertThat(directory).isEqualToComparingFieldByFieldRecursively(expectedDirectory);
    }

    @Test
    void moveBetweenDifferentUsers() {
        //Given
        Directory directoryWithAnotherOwner = createDirectory(321L, SEPARATOR + "anotherDirectory");
        //When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.move(directory, directoryWithAnotherOwner)
        );
    }

    @Test
    void moveTargetDirectoryContainsSubDirectoryWithSameName() {
        //Given
        Directory target = createDirectory(321L, SEPARATOR + "targetToMove", user);
        Directory targetSubDirectory = createDirectory(322L, target.getPath() + SEPARATOR + directory.getName(), user);

        when(directoryJpaRepository.findByOwnerAndPath(user, targetSubDirectory.getPath()))
                .thenReturn(singletonList(targetSubDirectory));

        //When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.move(directory, target)
        );

        //Then
        verify(directoryJpaRepository).findByOwnerAndPath(user, targetSubDirectory.getPath());
    }

    @Test
    void moveToSubDirectory() throws DirectoryModificationException {
        //Given
        Directory subDirectory = createDirectory(121L, buildPath(directory.getPath(), "subDir"), user);

        //When
        DirectoryModificationException directoryModificationException = assertThrows(DirectoryModificationException.class,
                () -> instance.move(directory, subDirectory)
        );
    }

    @Test
    void moveSuccess() {
        //Given
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        List<Directory> subDirectories = createSubdirectoryList(directory, subDirectoryNames);
        String targetPath = SEPARATOR + "great" + SEPARATOR + "my" + SEPARATOR + "directory";
        Directory target = createDirectory(999L, targetPath, directory.getOwner());

        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(unmodifiableList(subDirectories));

        //When
        instance.move(directory, target);

        //Then
        assertThat(subDirectories)
                .isNotNull()
                .hasSameSizeAs(subDirectoryNames);

        String expectedPath = targetPath + SEPARATOR + directory.getName();
        assertThat(directory.getPath()).isEqualTo(expectedPath);

        for (int i = 0; i < subDirectories.size(); i++) {
            Directory subDir = subDirectories.get(i);
            assertThat(subDir.getOwner())
                    .isEqualToComparingFieldByField(user);
            String expectedSubDirectoryPath = expectedPath + SEPARATOR + subDirectoryNames[i];
            assertThat(subDir.getPath())
                    .isEqualTo(expectedSubDirectoryPath);
        }
    }

    @Test
    void findTopLevelSubDirectoriesForRoot() {
        // Given
        Directory root = new DirectoryBuilder().owner(user).path(SEPARATOR).build();
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        List<Directory> subDirectories = createSubdirectoryList(root, subDirectoryNames);
        when(directoryJpaRepository
                .findByOwnerAndPathStartingWithAndLevel(root.getOwner(), root.getPath(), root.getLevel() + 1)
        ).thenReturn(subDirectories);
        List<Directory> expectedSubDirectories = createSubdirectoryList(root, subDirectoryNames);

        // When
        List<Directory> result = instance.findTopLevelSubDirectories(root);

        // Then
        assertThat(result).containsExactlyInAnyOrder(expectedSubDirectories.toArray(new Directory[0]));
    }

    @Test
    void findTopLevelSubDirectoriesForNonRoot() {
        // Given
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        List<Directory> subDirectories = createSubdirectoryList(directory, subDirectoryNames);
        when(directoryJpaRepository
                .findByOwnerAndPathStartingWithAndLevel(directory.getOwner(), directory.getPath() + SEPARATOR, directory.getLevel() + 1)
        ).thenReturn(subDirectories);
        List<Directory> expectedSubDirectories = createSubdirectoryList(directory, subDirectoryNames);

        // When
        List<Directory> result = instance.findTopLevelSubDirectories(directory);

        // Then
        assertThat(result).containsExactlyInAnyOrder(expectedSubDirectories.toArray(new Directory[0]));
    }

    @Test
    void countByOwner() {
        //Given
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        List<Directory> directories = createSubdirectoryList(directory, subDirectoryNames);
        directories.add(root);
        directories.add(directory);
        when(instance.countByOwner(user))
                .thenReturn((long) directories.size());

        //When
        long count = instance.countByOwner(user);

        //Then
        assertThat(count).isEqualTo(directories.size());
    }

    @Test
    void findByOwnerAndPathSuccess() {
        //Given
        Directory expected = new DirectoryBuilder().from(directory).build();
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));

        //When
        List<Directory> result = instance.findByOwnerAndPath(directory.getOwner(), directory.getPath());

        //Then
        verify(directoryJpaRepository).findByOwnerAndPath(expected.getOwner(), expected.getPath());
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(expected);
    }

    @Test
    void findByOwnerAndPathFailure() {
        //Given
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(emptyList());

        //When
        List<Directory> result = instance.findByOwnerAndPath(directory.getOwner(), directory.getPath());

        //Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void findByOwnerAndPathStartingWithSuccess() {
        //Given
        Directory expected = new DirectoryBuilder().from(directory).build();
        Directory expectedRoot = new DirectoryBuilder().from(root).build();
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(root.getOwner(), root.getPath()))
                .thenReturn(asList(root, directory));

        //When
        List<Directory> result = instance.findByOwnerAndPathStartingWith(root.getOwner(), root.getPath());

        //Then
        verify(directoryJpaRepository).findByOwnerAndPathStartingWith(expectedRoot.getOwner(), expectedRoot.getPath());
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(expectedRoot, expected);
    }

    @Test
    void findByOwnerAndPathStartingWithFailure() {
        //Given
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath()))
                .thenReturn(emptyList());

        //When
        List<Directory> result = instance.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath());

        //Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void findByIdSuccess() {
        //Given
        Directory expected = new DirectoryBuilder().from(directory).build();
        when(directoryJpaRepository.findById(directory.getId()))
                .thenReturn(Optional.ofNullable(directory));

        //When
        Optional<Directory> result = instance.findById(directory.getId());

        //Then
        verify(directoryJpaRepository).findById(expected.getId());
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(expected);
    }

    @Test
    void findByIdFailure() {
        //Given
        when(directoryJpaRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        long id = 123L;

        //When
        Optional<Directory> result = instance.findById(id);

        //Then
        verify(directoryJpaRepository).findById(id);
        assertThat(result)
                .isNotNull()
                .isNotPresent();
    }


    @Test
    void findByOwnerAndIdSuccess() {
        //Given
        Directory expected = new DirectoryBuilder().from(directory).build();
        when(directoryJpaRepository.findByOwnerAndId(directory.getOwner(), directory.getId()))
                .thenReturn(Optional.ofNullable(directory));

        //When
        Optional<Directory> result = instance.findByOwnerAndId(directory.getOwner(), directory.getId());

        //Then
        verify(directoryJpaRepository).findByOwnerAndId(expected.getOwner(), expected.getId());
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(expected);
    }

    @Test
    void findByOwnerAndIdFailure() {
        //Given
        when(directoryJpaRepository.findByOwnerAndId(any(UserInfo.class), any(Long.class)))
                .thenReturn(Optional.empty());
        long id = 123L;

        //When
        Optional<Directory> result = instance.findByOwnerAndId(user, id);

        //Then
        verify(directoryJpaRepository).findByOwnerAndId(user, id);
        assertThat(result)
                .isNotNull()
                .isNotPresent();

    }

    @Test
    void directoryExists() {
        //Given
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));
        //When
        boolean result = instance.exists(directory.getOwner(), directory.getPath());

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void directoryNotExists() {
        //Given
        String path = "/phantomDirectory";
        when(directoryJpaRepository.findByOwnerAndPath(user, path))
                .thenReturn(emptyList());

        //When
        boolean result = instance.exists(user, path);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void OperationArgumentsVOEquals() {
        EqualsVerifier.forClass(DirectoryDatabaseRepositoryImpl.OperationArgumentsVO.class)
                .usingGetClass()
                .verify();
    }
}