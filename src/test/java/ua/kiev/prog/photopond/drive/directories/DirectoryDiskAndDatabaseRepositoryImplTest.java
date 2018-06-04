package ua.kiev.prog.photopond.drive.directories;

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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

        dropAndCreateDirectoryOnDisk(basedirPath);

        user = new UserInfoBuilder().login("awesomeUser").role(UserRole.USER).build();
        root = createDirectory(1L, "/", user);
        directory = createDirectory(7L, "/first", user);
        directoryPathOnDisk = Paths.get(foldersBasedir + directory.getFullPath());
    }

    private void dropAndCreateDirectoryOnDisk(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(path)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.createDirectories(path);
        }
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
    public void saveNullDirectory() throws Exception {
        //When
        instance.save(null);
    }

    @Test
    public void saveWhenDirectoryNotExists() throws Exception {
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

        //When
        instance.save(directory);

        //Then
        assertThat(Files.exists(directoryPathOnDisk)).isTrue();
        verify(directoryJpaRepository).save(directory);
    }

    @Test(expected = DirectoryModificationException.class)
    public void saveWhenParentDirectoryNotExists() throws Exception {
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
    public void deleteNullDirectory() throws Exception {
        //When
        instance.delete(null);
    }

    @Test
    public void deleteEmptyDirectory() throws Exception {
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
    public void deleteWithSubdirectories() throws Exception {
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
    public void deleteWithJpaRepositoryException() throws Exception {
        //Given

        doThrow(QueryTimeoutException.class).when(directoryJpaRepository).deleteAll(singletonList(directory));

        try {
            //When
            instance.delete(directory);
        } catch (DirectoryModificationException e) {
            //Then
            verify(directoryJpaRepository).deleteAll(singletonList(directory));
            assertThat(Files.exists((directoryPathOnDisk))).isTrue();
            throw new DirectoryModificationException();
        }
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameNotExistsDirectory() throws Exception {
        // When
        instance.rename(directory, "someName");
    }

    @Test
    public void renameSuccess() throws Exception {
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
    public void renameWhenTargetDirectoryAlreadyExistsInDataBase() throws Exception {
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
    public void moveNullDirectory() throws Exception {
        //When
        instance.move(null, directory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void moveToNullDirectory() throws Exception {
        //When
        instance.move(directory, null);
    }

    @Test
    public void moveTargetDirectoryEqualsCurrentDirectory() throws Exception {
        //Given
        Directory expectedDirectory = new DirectoryBuilder().from(directory).build();

        //When
        instance.move(directory, directory);

        //Then
        assertThat(directory).isEqualToComparingFieldByFieldRecursively(expectedDirectory);
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveBetweenDifferentUsers() throws Exception {
        //Given
        Directory directoryWithAnotherOwner = createDirectory(321L, SEPARATOR + "anotherDirectory");
        //When
        instance.move(directory, directoryWithAnotherOwner);
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveTargetDirectoryContainsSubDirectoryWithSameName() throws Exception {
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
    public void moveSuccess() throws Exception {
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
    public void findSubDirectoriesForRoot() {
        // Given
        Directory root = new DirectoryBuilder().owner(user).path(SEPARATOR).build();
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        List<Directory> subDirectories = createSubdirectoryList(root, subDirectoryNames);
        when(directoryJpaRepository
                .findByOwnerAndPathStartingWithAndLevel(root.getOwner(), root.getPath(), root.getLevel() + 1)
        ).thenReturn(subDirectories);
        List<Directory> expectedSubDirectories = createSubdirectoryList(root, subDirectoryNames);

        // When
        List<Directory> result = instance.findTopSubDirectories(root);

        // Then
        assertThat(result).containsExactlyInAnyOrder(expectedSubDirectories.toArray(new Directory[0]));
    }

    @Test
    public void findSubDirectoriesForNonRoot() {
        // Given
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        List<Directory> subDirectories = createSubdirectoryList(directory, subDirectoryNames);
        when(directoryJpaRepository
                .findByOwnerAndPathStartingWithAndLevel(directory.getOwner(), directory.getPath() + SEPARATOR, directory.getLevel() + 1)
        ).thenReturn(subDirectories);
        List<Directory> expectedSubDirectories = createSubdirectoryList(directory, subDirectoryNames);

        // When
        List<Directory> result = instance.findTopSubDirectories(directory);

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
}