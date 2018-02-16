package ua.kiev.prog.photopond.drive.directories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
@ComponentScan(value = "ua.kiev.prog.photopond.drive")
public class DirectoryDiskAndDatabaseRepositoryImplTest {

    @MockBean
    private DirectoryJpaRepository directoryJpaRepository;

    private DirectoryDiskAndDatabaseRepositoryImpl instance;

    @Value(value = "${folders.basedir}")
    private String foldersBasedir;

    private Path foldersBasedirPath;
    private UserInfo user;
    private Directory directory;
    private Path directoryPathOnDisk;


    @Before
    public void setUp() throws Exception {
        instance = new DirectoryDiskAndDatabaseRepositoryImpl(directoryJpaRepository);
        instance.setFoldersBasedir(foldersBasedir);
        foldersBasedirPath = Paths.get(foldersBasedir);
        user = new UserInfoBuilder().login("awesomeUser").role(UserRole.USER).build();
        directory = new DirectoryBuilder().path("/first").owner(user).build();
        directoryPathOnDisk = Paths.get(foldersBasedir + "/" + user.getLogin() + "/first");


        if (Files.exists(foldersBasedirPath)) {
            Files.walkFileTree(foldersBasedirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(foldersBasedirPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.createDirectories(foldersBasedirPath);
        }
    }

    @Test
    public void isBasedirExists() throws Exception {
        assertThat(Files.exists(foldersBasedirPath)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNullDirectory() throws Exception {
        instance.save(null);
    }

    @Test
    public void saveWhenDirectoryNotExists() throws Exception {
        instance.save(directory);

        assertThat(Files.exists(directoryPathOnDisk)).isTrue();
        verify(directoryJpaRepository).save(directory);
    }

    @Test
    public void saveWhenDirectoryExists() throws Exception {
        Files.createDirectories(directoryPathOnDisk);

        instance.save(directory);

        assertThat(Files.exists(directoryPathOnDisk)).isTrue();
        verify(directoryJpaRepository).save(directory);
    }

    @Test(expected = DirectoryModificationException.class)
    public void saveWhenParentDirectoryNotExists() throws Exception {
        String parentPath = SEPARATOR + "First";
        directory.setPath(parentPath + SEPARATOR + "second");
        when(directoryJpaRepository.findByOwnerAndPath(user, parentPath)).thenReturn(Collections.<Directory>emptyList());

        try {
            instance.save(directory);
        } catch (DirectoryModificationException e) {
            verify(directoryJpaRepository).findByOwnerAndPath(user, directory.getParentPath());
            throw new DirectoryModificationException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullDirectory() throws Exception {
        instance.delete(null);
    }

    @Test
    public void delete() throws Exception {
        if (!Files.exists(directoryPathOnDisk)) {
            Files.createDirectories(directoryPathOnDisk);
        }

        instance.delete(directory);

        verify(directoryJpaRepository).delete(directory);
        assertThat(Files.exists(directoryPathOnDisk)).isFalse();
    }

    @Test(expected = DirectoryModificationException.class)
    public void deleteWithJpaRepositoryException() throws Exception {
        if (!Files.exists(directoryPathOnDisk)) {
            Files.createDirectories(directoryPathOnDisk);
        }

        doThrow(DirectoryModificationException.class).when(directoryJpaRepository).delete(directory);

        try {
            instance.delete(directory);
        } catch (DirectoryModificationException e) {
            verify(directoryJpaRepository).delete(directory);
            assertThat(Files.exists((directoryPathOnDisk))).isTrue();
            throw new DirectoryModificationException();
        }
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameNotExistsDirectory() throws Exception {
        instance.rename(directory, "someName");
    }

    @Test
    public void rename() throws Exception {
        final String subFolderName = "smthg";
        Directory subDirectory = new DirectoryBuilder()
                .owner(directory.getOwner())
                .id(765L)
                .path(directory.getPath() + SEPARATOR + subFolderName)
                .build();
        Directory notRenameDirectory = new DirectoryBuilder()
                .owner(user)
                .id(767L)
                .path(directory.getPath() + "TAIL")
                .build();
        if (!Files.exists(directoryPathOnDisk)) {
            Files.createDirectories(directoryPathOnDisk);
            Files.createDirectories(Paths.get(foldersBasedir + subDirectory.getFullPath()));
        }
        String currentPath = directory.getPath();
        String newName = "second";
        String targetPath = directory.getParentPath() + SEPARATOR + newName;
        String targetFullPath = foldersBasedirPath + directory.getOwnerFolder() + SEPARATOR + targetPath;
        Path targetPathOnDisk = Paths.get(targetFullPath);
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(user, currentPath))
                .thenReturn(asList(directory, subDirectory, notRenameDirectory));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(user, currentPath + SEPARATOR))
                .thenReturn(asList(subDirectory));

        instance.rename(directory, newName);

        assertThat(Files.exists(directoryPathOnDisk)).isFalse();
        assertThat(Files.exists(targetPathOnDisk)).isTrue();
        verify(directoryJpaRepository).findByOwnerAndPathStartingWith(directory.getOwner(), currentPath + SEPARATOR);
        assertThat(directory.getPath()).isEqualTo(targetPath);
        assertThat(subDirectory.getPath()).isEqualTo(targetPath + SEPARATOR + subFolderName);
        assertThat(notRenameDirectory.getPath()).isEqualTo(currentPath + "TAIL");
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameNotExistsOnDiskDirectory() throws Exception {
        Files.deleteIfExists(directoryPathOnDisk);
        List<Directory> directoriesForRenaming = asList(directory);
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(user, directory.getPath())).thenReturn(directoriesForRenaming);

        instance.rename(directory, "newName");
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameNewNameContainsSeparator() throws Exception {
        if (!Files.exists(directoryPathOnDisk)) {
            Files.createDirectories(directoryPathOnDisk);
        }

        instance.rename(directory, "new" + SEPARATOR + "Name");
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameWhenTargetDirectoryAlreadyExistsOnDisk() throws Exception {
        if (!Files.exists(directoryPathOnDisk)) {
            Files.createDirectories(directoryPathOnDisk);
        }
        String newName = "anotherDir";
        String targetPath = directory.getParentPath() + SEPARATOR + newName;
        String targetFullPath = foldersBasedirPath + directory.getOwnerFolder() + SEPARATOR + targetPath;
        Path targetPathOnDisk = Paths.get(targetFullPath);
        if (!Files.exists(targetPathOnDisk)) {
            Files.createDirectories(targetPathOnDisk);
        }

        instance.rename(directory, newName);
    }

    @Test(expected = DirectoryModificationException.class)
    public void renameWhenTargetDirectoryAlreadyExistsInDataBase() throws Exception {
        if (!Files.exists(directoryPathOnDisk)) {
            Files.createDirectories(directoryPathOnDisk);
        }
        String newName = "anotherDir";
        String targetPath = directory.getParentPath() + SEPARATOR + newName;
        String targetFullPath = foldersBasedirPath + directory.getOwnerFolder() + SEPARATOR + targetPath;
        Path targetPathOnDisk = Paths.get(targetFullPath);
        Files.deleteIfExists(targetPathOnDisk);
        Directory targetDirectory = new DirectoryBuilder().owner(user).path(targetPath).build();
        when(directoryJpaRepository.findByOwnerAndPath(user, targetPath))
                .thenReturn(asList(targetDirectory));

        instance.rename(directory, newName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void moveNullDirectory() throws Exception {
        instance.move(null, directory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void moveToNullDirectory() throws Exception {
        instance.move(directory, null);
    }

    @Test
    public void moveTargetDirectoryEqualsCurrentDirectory() throws Exception {
        Directory expectedDirectory = new DirectoryBuilder().owner(user).path(directory.getPath()).id(directory.getId()).build();
        instance.move(directory, directory);

        assertThat(directory).isEqualToComparingFieldByFieldRecursively(expectedDirectory);
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveBetweenDifferentUsers() throws Exception {
        Directory directoryWithAnotherOwner = new DirectoryBuilder()
                .path(SEPARATOR + "anotherDirectory")
                .owner(new UserInfo("hiddenUser", "qwerty123"))
                .build();

        instance.move(directory, directoryWithAnotherOwner);
    }

    @Test(expected = DirectoryModificationException.class)
    public void moveTargetDirectoryContainsSameDirectoryNameInDatabase() throws Exception {
        Directory target = new DirectoryBuilder()
                .owner(user)
                .path(SEPARATOR + "targetToMove")
                .id(321L)
                .build();
        Directory targetSubDirectory = new DirectoryBuilder()
                .owner(user)
                .path(target.getPath() + SEPARATOR + directory.getName())
                .id(322L)
                .build();
        when(directoryJpaRepository.findByOwnerAndPath(user, targetSubDirectory.getPath()))
                .thenReturn(asList(targetSubDirectory));


        try {
            instance.move(directory, target);
        } catch (DirectoryModificationException e) {
            verify(directoryJpaRepository).findByOwnerAndPath(user, targetSubDirectory.getPath());
            throw new DirectoryModificationException();
        }
    }

    @Test
    public void moveSuccess() throws Exception {
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        Directory[] subDirectories = createSubdirectory(directory, subDirectoryNames);
        String sourceDirectoryName = directory.getName();
        String targetPath = SEPARATOR + "great" + SEPARATOR + "my" + SEPARATOR + "directory";
        Directory target = new DirectoryBuilder()
                .id(999L)
                .owner(directory.getOwner())
                .path(targetPath)
                .build();
        Path targetPathOnDisk = Paths.get(foldersBasedirPath + target.getFullPath());
        if (!Files.exists(targetPathOnDisk)) {
            Files.createDirectories(targetPathOnDisk);
        }
        when(directoryJpaRepository.findByOwnerAndPath(directory.getOwner(), directory.getPath()))
                .thenReturn(asList(directory));
        when(directoryJpaRepository.findByOwnerAndPathStartingWith(directory.getOwner(), directory.getPath() + SEPARATOR))
                .thenReturn(asList(subDirectories));


        instance.move(directory, target);

        String expectedPath = targetPath + SEPARATOR + sourceDirectoryName;
        assertThat(directory.getPath()).isEqualTo(expectedPath);
        for (int i = 0; i < subDirectoryNames.length; i++) {
            assertThat(subDirectories[i].getOwner()).isEqualToComparingFieldByField(user);
            String expectedSubDirectoryPath = expectedPath + SEPARATOR + subDirectoryNames[i];
            assertThat(subDirectories[i].getPath()).isEqualTo(expectedSubDirectoryPath);
        }

    }

    private Directory[] createSubdirectory(Directory directory, String[] subDirectoryNames) throws IOException {
        Directory[] arr = new Directory[subDirectoryNames.length];
        for (int i = 0; i < arr.length; i++) {
            String directoryPath;
            if(directory.isRoot()) {
                directoryPath = SEPARATOR + subDirectoryNames[i];
            } else {
                directoryPath = directory.getPath() + SEPARATOR + subDirectoryNames[i];
            }
            arr[i] = new DirectoryBuilder()
                    .id(1001L + i)
                    .owner(user)
                    .path(directoryPath)
                    .build();
            Path path = Paths.get(foldersBasedir + arr[i].getFullPath());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
        return arr;
    }

    @Test
    public void findSubDirectoriesForRoot() throws Exception {
        Directory root = new DirectoryBuilder().owner(user).path(SEPARATOR).build();
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        Directory[] subDirectories = createSubdirectory(root, subDirectoryNames);
        when(directoryJpaRepository
                .findByOwnerAndPathStartingWithAndLevel(root.getOwner(), root.getPath(), root.getLevel() + 1)
        ).thenReturn(asList(subDirectories));
        Directory[] expectedSubDirectories = createSubdirectory(root, subDirectoryNames);

        List<Directory> result = instance.findTopSubDirectories(root);

        assertThat(result).containsExactlyInAnyOrder(expectedSubDirectories);
    }

    @Test
    public void findSubDirectoriesForNonRoot() throws Exception {
        String[] subDirectoryNames = {"qwerty", "New folder", "New folder (2)"};
        Directory[] subDirectories = createSubdirectory(directory, subDirectoryNames);
        when(directoryJpaRepository
                .findByOwnerAndPathStartingWithAndLevel(directory.getOwner(), directory.getPath() + SEPARATOR, directory.getLevel() + 1)
        ).thenReturn(asList(subDirectories));
        Directory[] expectedSubDirectories = createSubdirectory(directory, subDirectoryNames);

        List<Directory> result = instance.findTopSubDirectories(directory);

        assertThat(result).containsExactlyInAnyOrder(expectedSubDirectories);

    }
}