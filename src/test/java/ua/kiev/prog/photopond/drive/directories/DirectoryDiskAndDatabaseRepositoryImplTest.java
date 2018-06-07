package ua.kiev.prog.photopond.drive.directories;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
public class DirectoryDiskAndDatabaseRepositoryImplTest {

    @MockBean
    private DirectoryJpaRepository directoryJpaRepository;

    private DirectoryDiskAndDatabaseRepositoryImpl instance;

    @Value(value = "${folders.basedir.location}")
    private String foldersBasedir;

    private Path basedirPath;
    private UserInfo user;
    private Directory root;
    private Directory directory;
    private Path directoryPathOnDisk;


    @Before
    public void setUp() throws Exception {
        instance = new DirectoryDiskAndDatabaseRepositoryImpl(directoryJpaRepository);
        instance.setFoldersBasedir(foldersBasedir);
        basedirPath = Paths.get(foldersBasedir);

        FileUtils.cleanDirectory(basedirPath.toFile());

        user = new UserInfoBuilder().login("awesomeUser").role(UserRole.USER).build();
        root = createDirectory(1L, "/", user);
        directory = createDirectory(7L, "/first", user);
        directoryPathOnDisk = Paths.get(foldersBasedir + directory.getFullPath());
    }

    private List<Directory> createSubdirectoryList(Directory directory, String... subDirectoryNames) {
        AtomicLong id = new AtomicLong(1001L);

        return Arrays.stream(subDirectoryNames)
                .map(name -> buildPath(directory.getPath(), name))
                .map(p -> createDirectory(id.getAndIncrement(), p, user))
                .collect(Collectors.toList());
    }

    private Directory createDirectory(Long id, String path, UserInfo owner) {
        Directory directory = new DirectoryBuilder()
                .id(id)
                .owner(owner)
                .path(path)
                .build();
        Path pathOnDisk = Paths.get(foldersBasedir + directory.getFullPath());
        if (!Files.exists(pathOnDisk)) {
            try {
                Files.createDirectories(pathOnDisk);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return directory;
    }

    private Directory createDirectory(Long id, String path) {
        UserInfo anotherOwner = new UserInfo("hiddenUser", "qwerty123");
        return createDirectory(id, path, anotherOwner);
    }

    @Test
    public void isBasedirExists() {
        //Then
        assertThat(Files.exists(basedirPath)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNullDirectory() {
        //When
        instance.save(null);
    }

    @Test
    public void saveWhenDirectoryNotExists() {
        //Given
        when(directoryJpaRepository.save(any(Directory.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        instance.save(directory);

        //Then
        assertThat(Files.exists(directoryPathOnDisk)).isTrue();
        verify(directoryJpaRepository).save(directory);
    }

    @Test
    public void saveWhenDirectoryExists() throws Exception {
        //Given
        Files.createDirectories(directoryPathOnDisk);
        when(directoryJpaRepository.save(any(Directory.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        instance.save(directory);

        //Then
        assertThat(Files.exists(directoryPathOnDisk)).isTrue();
        verify(directoryJpaRepository).save(directory);
    }

    @Test(expected = DirectoryModificationException.class)
    public void saveWhenParentDirectoryNotExists() {
        //Given
        String parentPath = buildPath("First");
        directory.setPath(buildPath(parentPath, "second"));
        when(directoryJpaRepository.findByOwnerAndPath(user, parentPath)).thenReturn(emptyList());

        try {
            //When
            instance.save(directory);
        } catch (DirectoryModificationException e) {
            //Then
            verify(directoryJpaRepository).findByOwnerAndPath(user, directory.parentPath());
            throw new DirectoryModificationException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullDirectory() {
        //When
        instance.delete(null);
    }

    @Test
    public void deleteEmptyDirectory() {
        //Given
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(new LinkedList<>());

        //When
        instance.delete(directory);

        //Then
        verify(directoryJpaRepository).deleteAll(singletonList(directory));
        assertThat(Files.exists(directoryPathOnDisk)).isFalse();
    }

    @Test
    public void deleteWithSubdirectories() {
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
        for (Directory d : deleted) {
            assertThat(Files.exists(Paths.get(foldersBasedir + d.getFullPath()))).isFalse();
        }
    }

    @Test(expected = DirectoryModificationException.class)
    public void deleteWithJpaRepositoryException() {
        //Given
        doThrow(QueryTimeoutException.class).when(directoryJpaRepository).deleteAll(singletonList(directory));

        try {
            //When
            instance.delete(directory);
        } catch (DirectoryModificationException e) {
            //Then
            verify(directoryJpaRepository).deleteAll(singletonList(directory));
            assertThat(Files.exists(directoryPathOnDisk)).isTrue();
            throw e;
        }
    }

    @Test
    public void renameTargetEqualsSource() throws DirectoryModificationException {
        //Given
        Directory expected = new DirectoryBuilder().from(directory).build();

        //When
        instance.rename(directory, directory.getPath());

        //Then
        verify(directoryJpaRepository, never()).save(any(Directory.class));
        verify(directoryJpaRepository, never()).findByOwnerAndPathStartingWith(any(UserInfo.class), any(String.class));
        assertThat(directory).isEqualTo(expected);
        assertThat(Files.exists(Paths.get(basedirPath + expected.getFullPath()))).isTrue();
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameNotExistsDirectory() {
        // When
        instance.rename(directory, "someName");
    }

    @Test
    public void renameSuccess() {
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
        assertThat(Files.exists(
                Paths.get(basedirPath + directoryInSameLevel.getFullPath())
        )).isTrue();

        assertThat(targetNames).hasSameSizeAs(sourceNames);
        assertThat(targetNames).hasSameSizeAs(directories);
        for (int i = 0; i < targetNames.length; i++) {
            assertThat(Files.exists(
                    Paths.get(basedirPath + SEPARATOR + user.getLogin() + sourceNames[i])
            )).isFalse();
            assertThat(Files.exists(
                    Paths.get(basedirPath + SEPARATOR + user.getLogin() + targetNames[i])
            )).isTrue();
            assertThat(directories.get(i).getPath()).isEqualTo(targetNames[i]);
        }
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameNotExistsOnDiskDirectory() throws Exception {
        //Given
        Files.deleteIfExists(directoryPathOnDisk);
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.parentPath()))
                .thenReturn(singletonList(root));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(emptyList());

        //When
        instance.rename(directory, buildPath("newName"));
    }

    @Test
    public void renameWhenTargetDirectoryAlreadyExistsOnDisk() throws Exception {
        //Given
        String newName = "anotherDir";
        String targetPath = buildPath(directory.parentPath(), newName);
        Path targetPathOnDisk = Paths.get(basedirPath + buildPath(directory.getOwnerFolder(), targetPath));
        if (!Files.exists(targetPathOnDisk)) {
            Files.createDirectories(targetPathOnDisk);
        }
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(singletonList(directory));
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.parentPath()))
                .thenReturn(singletonList(root));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(emptyList());

        //When
        instance.rename(directory, targetPath);

        //Then
        assertThat(Files.exists(targetPathOnDisk)).isTrue();
        assertThat(Files.exists(directoryPathOnDisk)).isFalse();
        assertThat(directory.getPath()).isEqualTo(targetPath);

    }

    @Test(expected = DirectoryModificationException.class)
    public void renameWhenTargetDirectoryAlreadyExistsInDataBase() {
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
        instance.rename(this.directory, targetPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void moveNullDirectory() {
        //When
        instance.move(null, directory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void moveToNullDirectory() {
        //When
        instance.move(directory, null);
    }

    @Test
    public void moveTargetDirectoryEqualsCurrentDirectory() {
        //Given
        Directory expectedDirectory = new DirectoryBuilder().from(directory).build();

        //When
        instance.move(directory, directory);

        //Then
        assertThat(directory).isEqualToComparingFieldByFieldRecursively(expectedDirectory);
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveBetweenDifferentUsers() {
        //Given
        Directory directoryWithAnotherOwner = createDirectory(321L, SEPARATOR + "anotherDirectory");
        //When
        instance.move(directory, directoryWithAnotherOwner);
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveTargetDirectoryContainsSubDirectoryWithSameName() {
        //Given
        Directory target = createDirectory(321L, SEPARATOR + "targetToMove", user);
        Directory targetSubDirectory = createDirectory(322L, target.getPath() + SEPARATOR + directory.getName(), user);

        when(directoryJpaRepository.findByOwnerAndPath(user, targetSubDirectory.getPath()))
                .thenReturn(singletonList(targetSubDirectory));


        try {
            //When
            instance.move(directory, target);
        } catch (DirectoryModificationException e) {
            //Then
            verify(directoryJpaRepository).findByOwnerAndPath(user, targetSubDirectory.getPath());
            throw new DirectoryModificationException();
        }
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveToSubDirectory() throws DirectoryModificationException {
        //Given
        Directory subDirectory = createDirectory(121L, buildPath(directory.getPath(), "subDir"), user);

        //When
        instance.move(directory, subDirectory);
    }

    @Test
    public void moveSuccess() {
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
            assertThat(Files.exists(Paths.get(foldersBasedir + subDir.getFullPath())))
                    .isTrue();
        }
    }

    @Test
    public void findTopLevelSubDirectoriesForRoot() {
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
    public void findTopLevelSubDirectoriesForNonRoot() {
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
    public void countByOwner() {
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
    public void findByOwnerAndPathSuccess() {
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
    public void findByOwnerAndPathFailure() {
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
    public void findByIdSuccess() {
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
    public void findByIdFailure() {
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
    public void findByOwnerAndIdSuccess() {
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
    public void findByOwnerAndIdFailure() {
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
}