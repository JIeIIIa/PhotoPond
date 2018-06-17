package ua.kiev.prog.photopond.drive;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.Content.ContentBuilder;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryDiskAndDatabaseRepository;
import ua.kiev.prog.photopond.drive.directories.DirectoryException;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileException;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
public class DriveServiceImplTest {
    @MockBean
    private DirectoryDiskAndDatabaseRepository directoryRepository;

    @MockBean
    private PictureFileRepository fileRepository;

    @MockBean
    private UserInfoService userInfoService;

    private DriveService instance;

    private UserInfo user;

    private Directory root;

    @Before
    public void setUp() {
        instance = new DriveServiceImpl(directoryRepository, fileRepository, userInfoService);
        user = new UserInfoBuilder()
                .id(123L)
                .login("someUser")
                .password("qwerty123!")
                .build();
        root = new DirectoryBuilder()
                .owner(user)
                .path(SEPARATOR)
                .build();
    }

    private void callMoveDirectoryWhenMockMoveAndRenameNotInvocation(String sourcePath, String targetPath) {
        try {
            instance.moveDirectory(user.getLogin(), sourcePath, targetPath);
        } catch (DriveException e) {
            //Then
            verify(directoryRepository, never()).move(any(Directory.class), any(Directory.class));
            verify(directoryRepository, never()).rename(any(Directory.class), anyString());

            throw e;
        }
    }

    @Test
    public void userHasNoDirectory() {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .from(root)
                .build();
        Content expectedContent = ContentBuilder.getInstance()
                .currentDirectory(root)
                .build();

        when(directoryRepository.countByOwner(user)).thenReturn(0L);
        when(directoryRepository.findByOwnerAndPath(user, SEPARATOR))
                .thenReturn(emptyList())
                .thenReturn(emptyList())
                .thenReturn(singletonList(root));
        when(directoryRepository.save(any(Directory.class)))
                .thenAnswer(answer -> answer.getArguments()[0]);
        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        //When
        Content result = instance.retrieveDirectoryContent(user.getLogin(), SEPARATOR);

        //Then
        ArgumentCaptor<Directory> rootDirectory = ArgumentCaptor.forClass(Directory.class);
        verify(directoryRepository).save(rootDirectory.capture());
        assertThat(rootDirectory.getValue()).isEqualToIgnoringGivenFields(expectedRoot, "id");
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedContent);
    }

    @Test(expected = DriveException.class)
    public void userHasNoDirectoryAndFailuerCreating() {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .from(root)
                .build();

        when(directoryRepository.countByOwner(user)).thenReturn(0L);
        when(directoryRepository.findByOwnerAndPath(user, SEPARATOR))
                .thenReturn(emptyList())
                .thenReturn(emptyList());
        when(directoryRepository.save(any(Directory.class)))
                .thenThrow(new DirectoryException());
        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        //When
        try {
            instance.retrieveDirectoryContent(user.getLogin(), SEPARATOR);
        } catch (DriveException e) {
            //Then
            ArgumentCaptor<Directory> rootDirectory = ArgumentCaptor.forClass(Directory.class);
            verify(directoryRepository).save(rootDirectory.capture());
            assertThat(rootDirectory.getValue()).isEqualToIgnoringGivenFields(expectedRoot, "id");

            throw e;
        }
    }

    @Test
    public void rootDirectoryWithoutContent() {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .owner(user)
                .path(SEPARATOR)
                .build();
        when(directoryRepository.countByOwner(user)).thenReturn(1L);
        when(directoryRepository.findByOwnerAndPath(user, SEPARATOR)).thenReturn(singletonList(root));
        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.of(user));
        Content expectedContent = ContentBuilder.getInstance()
                .currentDirectory(root)
                .build();

        //When
        Content content = instance.retrieveDirectoryContent(user.getLogin(), SEPARATOR);

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
        assertThat(content).isEqualToComparingFieldByFieldRecursively(expectedContent);
        assertThat(content.getCurrentDirectory()).isEqualToIgnoringGivenFields(expectedRoot, "id");
    }

    @Test
    public void rootDirectoryWithContent() {
        //Given
        Directory[] directories = new Directory[3];
        directories[0] = new DirectoryBuilder().owner(user).path(SEPARATOR).build();
        directories[1] = new DirectoryBuilder().owner(user).path(SEPARATOR + "first").build();
        directories[2] = new DirectoryBuilder().owner(user).path(directories[1].getPath() + SEPARATOR + "second").build();

        when(directoryRepository.countByOwner(user)).thenReturn(3L);
        when(directoryRepository.findByOwnerAndPath(user, directories[0].getPath()))
                .thenReturn(singletonList(
                        new DirectoryBuilder().from(directories[0]).build()
                ));
        when(directoryRepository.findByOwnerAndPath(user, directories[1].getPath()))
                .thenReturn(singletonList(
                        new DirectoryBuilder().from(directories[1]).build()
                ));
        when(directoryRepository.findByOwnerAndPath(user, directories[2].getPath()))
                .thenReturn(singletonList(
                        new DirectoryBuilder().from(directories[2]).build()
                ));
        Content expectedContent = ContentBuilder.getInstance()
                .currentDirectory(directories[2])
                .parents(asList(
                        new DirectoryBuilder().from(directories[0]).build(),
                        new DirectoryBuilder().from(directories[1]).build()
                ))
                .build();
        when(directoryRepository.findTopLevelSubDirectories(new DirectoryBuilder().from(directories[2]).build()))
                .thenReturn(new LinkedList<>());
        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.of(user));

        //When
        Content content = instance.retrieveDirectoryContent(user.getLogin(), directories[2].getPath());

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
        assertThat(content).isEqualToComparingFieldByFieldRecursively(expectedContent);
        assertThat(content.getCurrentDirectory()).isEqualToIgnoringGivenFields(directories[2], "id");
        assertThat(content.getTopSubDirectories())
                .isEmpty();
        assertThat(content.getParents())
                .containsExactly(directories[0], directories[1]);
    }

    @Test
    public void notRootDirectoryWithContent() {
        //Given
        Directory[] directories = new Directory[3];
        directories[0] = new DirectoryBuilder().owner(user).path(SEPARATOR).build();
        directories[1] = new DirectoryBuilder().owner(user).path(SEPARATOR + "first").build();
        directories[2] = new DirectoryBuilder().owner(user).path(directories[1].getPath() + SEPARATOR + "second").build();

        when(directoryRepository.countByOwner(user)).thenReturn(3L);
        when(directoryRepository.findByOwnerAndPath(user, directories[0].getPath()))
                .thenReturn(singletonList(
                        new DirectoryBuilder().from(directories[0]).build()
                ));
        when(directoryRepository.findByOwnerAndPath(user, directories[1].getPath()))
                .thenReturn(singletonList(
                        new DirectoryBuilder().from(directories[1]).build()
                ));
        when(directoryRepository.findByOwnerAndPath(user, directories[2].getPath()))
                .thenReturn(singletonList(
                        new DirectoryBuilder().from(directories[2]).build()
                ));
        Content expectedContent = ContentBuilder.getInstance()
                .currentDirectory(directories[1])
                .parents(singletonList(
                        new DirectoryBuilder().from(directories[0]).build()
                ))
                .topSubDirectories(singletonList(new DirectoryBuilder().from(directories[2]).build()))
                .build();

        when(directoryRepository.findTopLevelSubDirectories(new DirectoryBuilder().from(directories[1]).build()))
                .thenReturn(singletonList(new DirectoryBuilder().from(directories[2]).build()));
        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.of(user));

        //When
        Content content = instance.retrieveDirectoryContent(user.getLogin(), directories[1].getPath());

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
        assertThat(content).isEqualToComparingFieldByFieldRecursively(expectedContent);
        assertThat(content.getCurrentDirectory()).isEqualToIgnoringGivenFields(directories[1], "id");
        assertThat(content.getTopSubDirectories())
                .hasSize(1)
                .containsExactly(directories[2]);
        assertThat(content.getParents())
                .containsExactly(directories[0]);
    }

    @Test
    public void addDirectorySuccess() {
        //Given
        Directory expected = new DirectoryBuilder().path(buildPath(root.getPath(), "first")).owner(user).build();

        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
        when(directoryRepository.exists(user, expected.getPath())).thenReturn(false);
        when(directoryRepository.save(any(Directory.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        Directory result = instance.addDirectory(user.getLogin(), root.getPath(), expected.getName());

        //Then
        verify(directoryRepository).save(expected);
        assertThat(result).isEqualToIgnoringGivenFields(expected, "id");
    }

    @Test(expected = DriveException.class)
    public void addDirectoryWhenLoginFailure() {
        //Given
        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.empty());

        //When
        try {
            instance.addDirectory(user.getLogin(), root.getPath(), "/somePath");
        } catch (DriveException e) {
            //Then
            verify(directoryRepository, never()).save(any(Directory.class));
            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void addDirectoryWhenDirectoryAlreadyExists() {
        //Given
        String newDirectoryPath = buildPath(root.getPath(), "existsDirectory");
        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.exists(user, newDirectoryPath)).thenReturn(true);

        //When
        try {
            instance.addDirectory(user.getLogin(), root.getPath(), newDirectoryPath);
        } catch (DriveException e) {
            //Then
            verify(directoryRepository, never()).save(any(Directory.class));
            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void addDirectoryWhenDirectoryRepositoryThrowsException() {
        //Given
        Directory expected = new DirectoryBuilder().path(buildPath(root.getPath(), "first")).owner(user).build();

        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
        when(directoryRepository.exists(user, expected.getPath())).thenReturn(false);
        when(directoryRepository.save(any(Directory.class))).thenThrow(new DirectoryException());

        //When
        try {
            instance.addDirectory(user.getLogin(), root.getPath(), expected.getPath());
        } catch (DriveException e) {
            //Then
            verify(directoryRepository).save(expected);
            throw e;
        }
    }

    @Test
    public void moveDirectorySuccess() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();
        Directory expected = new DirectoryBuilder().owner(user).path(buildPath(target.getPath(), source.getName())).build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(singletonList(source));
        when(directoryRepository.findByOwnerAndPath(user, target.getPath()))
                .thenReturn(singletonList(target));
        when(directoryRepository.findByOwnerAndPath(user, expected.getPath()))
                .thenReturn(emptyList());
        doAnswer(invocationOnMock -> {
            Directory first = (Directory) invocationOnMock.getArguments()[0];
            Directory second = (Directory) invocationOnMock.getArguments()[1];
            first.setPath(buildPath(second.getPath(), first.getName()));

            return null;
        }).when(directoryRepository).move(any(Directory.class), any(Directory.class));


        //When
        instance.moveDirectory(user.getLogin(), source.getPath(), expected.getPath());

        //Then
        verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
        verify(directoryRepository).findByOwnerAndPath(user, targetPath);
        verify(directoryRepository).move(source, target);
        assertThat(source).isEqualToIgnoringGivenFields(expected, "id");
    }

    @Test(expected = DriveException.class)
    public void moveDirectoryWhenDirectoryRepositoryThrowsException() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();
        Directory expected = new DirectoryBuilder().owner(user).path(buildPath(target.getPath(), source.getName())).build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(singletonList(source));
        when(directoryRepository.findByOwnerAndPath(user, target.getPath()))
                .thenReturn(singletonList(target));
        when(directoryRepository.findByOwnerAndPath(user, expected.getPath()))
                .thenReturn(emptyList());
        doThrow(new DriveException())
                .when(directoryRepository).move(any(Directory.class), any(Directory.class));


        //When
        try {
            instance.moveDirectory(user.getLogin(), source.getPath(), expected.getPath());
        } catch (DriveException e) {
            //Then
            verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
            verify(directoryRepository).findByOwnerAndPath(user, targetPath);
            verify(directoryRepository).move(source, target);
            verify(directoryRepository, never()).rename(any(Directory.class), anyString());
            throw e;
        }
    }


    @Test(expected = DriveException.class)
    public void moveDirectoryWhenUserNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }

    @Test(expected = DriveException.class)
    public void moveDirectoryWhenSourceDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/first/second").build();
        String targetPath = target.getPath();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(emptyList());
        when(directoryRepository.findByOwnerAndPath(user, target.getPath()))
                .thenReturn(singletonList(target));

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }


    @Test(expected = DriveException.class)
    public void moveDirectoryWhenTargetDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(singletonList(source));
        when(directoryRepository.findByOwnerAndPath(user, target.getPath()))
                .thenReturn(emptyList());

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }

    @Test
    public void renameDirectorySuccess() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory parent = new DirectoryBuilder().owner(user).path("/first").build();
        String parentPath = parent.getPath();
        String targetPath = buildPath(parent.getPath(), "newName");
        Directory expected = new DirectoryBuilder().owner(user).path(targetPath).build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(singletonList(source));
        when(directoryRepository.findByOwnerAndPath(user, parent.getPath()))
                .thenReturn(singletonList(parent));
        when(directoryRepository.findByOwnerAndPath(user, expected.getPath()))
                .thenReturn(emptyList());
        doAnswer(invocationOnMock -> {
            Directory first = (Directory) invocationOnMock.getArguments()[0];
            String second = (String) invocationOnMock.getArguments()[1];
            first.setPath(buildPath(first.parentPath(), Directory.getName(second)));

            return null;
        }).when(directoryRepository).rename(any(Directory.class), anyString());


        //When
        instance.moveDirectory(user.getLogin(), source.getPath(), expected.getPath());

        //Then
        verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
        verify(directoryRepository).findByOwnerAndPath(user, parentPath);
        verify(directoryRepository).rename(source, targetPath);
        assertThat(source).isEqualToIgnoringGivenFields(expected, "id");
    }

    @Test(expected = DriveException.class)
    public void renameDirectoryWhenDirectoryRepositoryThrowsException() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory parent = new DirectoryBuilder().owner(user).path("/first").build();
        String parentPath = parent.getPath();
        String targetPath = buildPath(parent.getPath(), "newName");
        Directory expected = new DirectoryBuilder().owner(user).path(targetPath).build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(singletonList(source));
        when(directoryRepository.findByOwnerAndPath(user, parent.getPath()))
                .thenReturn(singletonList(parent));
        when(directoryRepository.findByOwnerAndPath(user, expected.getPath()))
                .thenReturn(emptyList());
        doThrow(new DirectoryException())
                .when(directoryRepository).rename(any(Directory.class), anyString());


        //When
        try {
            instance.moveDirectory(user.getLogin(), source.getPath(), expected.getPath());
        } catch (DriveException e) {
            //Then
            verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
            verify(directoryRepository).findByOwnerAndPath(user, parentPath);
            verify(directoryRepository).rename(source, targetPath);
            verify(directoryRepository, never()).move(any(Directory.class), any(Directory.class));

            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void renameDirectoryWhenSourceDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory parent = new DirectoryBuilder().owner(user).path("/first").build();
        String targetPath = buildPath(source.parentPath(), "another");

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(emptyList());
        when(directoryRepository.findByOwnerAndPath(user, parent.getPath()))
                .thenReturn(singletonList(parent));

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }

    @Test
    public void deleteDirectorySuccess() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourcePath))
                .thenReturn(singletonList(source));

        //When
        instance.deleteDirectory(user.getLogin(), sourcePath);

        //Then
        verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
        verify(directoryRepository).delete(source);
    }

    @Test(expected = DriveException.class)
    public void deleteDirectoryWhenOwnerNotExist() {
        //Given
        when(userInfoService.findUserByLogin(anyString()))
                .thenReturn(Optional.empty());

        //When
        try {
            instance.deleteDirectory(user.getLogin(), "/someDirectory");
        } catch (DriveException e) {
            verify(userInfoService).findUserByLogin(user.getLogin());
            verify(directoryRepository, never()).findByOwnerAndPath(any(UserInfo.class), anyString());
            verify(directoryRepository, never()).delete(any(Directory.class));

            throw e;
        }
    }

    @Test
    public void deleteDirectoryWhenSourceDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourcePath))
                .thenReturn(emptyList());

        //When
        try {
            instance.deleteDirectory(user.getLogin(), sourcePath);
        } catch (DriveException e) {
            //Then
            verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
            verify(directoryRepository, never()).delete(any(Directory.class));
        }
    }

    @Test
    public void retrievePictureFileDataSuccess() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        final byte[] DATA = filename.getBytes();
        String filePath = buildPath(directory.getPath(), filename);
        PictureFile pictureFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(filename)
                .data(filename.getBytes())
                .build();


        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, filename))
                .thenReturn(singletonList(PictureFileBuilder.getInstance().from(pictureFile).build()));

        //When
        byte[] result = instance.retrievePictureFileData(user.getLogin(), filePath);

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(DATA);
        verify(userInfoService).findUserByLogin(user.getLogin());
        verify(directoryRepository).findByOwnerAndPath(user, path);
        verify(fileRepository).findByDirectoryAndFilename(directory, filename);
    }

    @Test(expected = DriveException.class)
    public void retrievePictureFileDataWhenFoundTooManyFiles() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        String filePath = buildPath(directory.getPath(), filename);
        PictureFile pictureFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(filename)
                .build();


        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, filename))
                .thenReturn(asList(
                        PictureFileBuilder.getInstance().from(pictureFile).build(),
                        PictureFileBuilder.getInstance().from(pictureFile).build()
                ));

        //When
        instance.retrievePictureFileData(user.getLogin(), filePath);
    }

    @Test(expected = DriveException.class)
    public void retrievePictureFileDataWhenFileNotFound() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        String filePath = buildPath(directory.getPath(), filename);

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, filename))
                .thenReturn(emptyList());

        //When
        instance.retrievePictureFileData(user.getLogin(), filePath);
    }

    @Test(expected = DriveException.class)
    public void retrievePictureFileDataWhenFileRepositoryThrowsException() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        String filePath = buildPath(directory.getPath(), filename);

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        doThrow(new PictureFileException()).when(fileRepository).findByDirectoryAndFilename(directory, filename);

        //When
        instance.retrievePictureFileData(user.getLogin(), filePath);
    }

    @Test
    public void addPictureFileSuccess() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/someDirectory").build();
        String originalFilename = "awesomeFile.jpg";
        MultipartFile file = new MockMultipartFile("filename", originalFilename, "image/jpeg", originalFilename.getBytes());

        PictureFile expected = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath())).thenReturn(singletonList(directory));
        when(fileRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        PictureFile result = instance.addPictureFile(user.getLogin(), directory.getPath(), file);

        //Then
        verify(userInfoService).findUserByLogin(user.getLogin());
        verify(fileRepository).save(expected);
        assertThat(result)
                .isNotNull()
                .isEqualToIgnoringGivenFields(expected, "id");
    }

    @Test(expected = DriveException.class)
    public void addPictureFileWhenMultipartFileThrowsException() throws IOException {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/someDirectory").build();
        String originalFilename = "awesomeFile.jpg";

        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getBytes()).thenThrow(new IOException());

        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath())).thenReturn(singletonList(directory));

        //When
        try {
            instance.addPictureFile(user.getLogin(), directory.getPath(), file);
        } catch (DriveException e) {
            //Then
            verify(userInfoService).findUserByLogin(user.getLogin());
            verify(fileRepository, never()).save(any(PictureFile.class));

            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void addPictureFileWhenPictureFileRepositoryThrowsException() throws IOException {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/someDirectory").build();
        String originalFilename = "awesomeFile.jpg";

        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getBytes()).thenReturn(originalFilename.getBytes());

        when(userInfoService.findUserByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath())).thenReturn(singletonList(directory));
        when(fileRepository.save(any(PictureFile.class))).thenThrow(new PictureFileException());

        //When
        try {
            instance.addPictureFile(user.getLogin(), directory.getPath(), file);
        } catch (DriveException e) {
            //Then
            verify(userInfoService).findUserByLogin(user.getLogin());
            verify(fileRepository).save(any(PictureFile.class));

            throw e;
        }
    }

    @Test
    public void moveAndRenamePictureFileSuccess() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";
        String targetFilename = "newName.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .directory(sourceDirectory)
                .filename(filename)
                .data(filename.getBytes())
                .build();
        PictureFile expected = PictureFileBuilder.getInstance()
                .directory(targetDirectory)
                .filename(targetFilename)
                .data(filename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(sourceDirectory, filename))
                .thenReturn(singletonList(file));
        doAnswer(invocationOnMock -> {
            PictureFile pictureFile = (PictureFile) invocationOnMock.getArguments()[0];
            pictureFile.setDirectory((Directory) invocationOnMock.getArguments()[1]);
            pictureFile.setFilename((String) invocationOnMock.getArguments()[2]);

            return null;
        }).when(fileRepository).move(any(PictureFile.class), any(Directory.class), anyString());


        //When
        instance.movePictureFile(user.getLogin(), file.getPath(), expected.getPath());

        //Then
        assertThat(file).isEqualToIgnoringGivenFields(expected, "id");
    }

    @Test(expected = DriveException.class)
    public void movePictureFileWhenFileNotFound() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(sourceDirectory, filename))
                .thenReturn(emptyList());

        //When
        try {
            instance.movePictureFile(
                    user.getLogin(),
                    buildPath(sourceDirectory.getPath(), filename),
                    buildPath(targetDirectory.getPath(), filename)
            );
        }catch (DriveException e) {
            //Then
            verify(fileRepository, never()).move(any(PictureFile.class), any(Directory.class), anyString());

            throw e;
        }
    }


    @Test(expected = DriveException.class)
    public void movePictureWhenFoundTooManyFiles() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .directory(sourceDirectory)
                .filename(filename)
                .data(filename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(sourceDirectory, filename))
                .thenReturn(asList(file, file));

        //When
        try {
            instance.movePictureFile(user.getLogin(), file.getPath(), buildPath(targetDirectory.getPath(), filename));
        } catch (DriveException e) {
            //Then
            verify(fileRepository, never()).move(any(PictureFile.class), any(Directory.class), anyString());

            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void movePictureWhenPictureFileRepositoryThrowsExceptionOnFind() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(any(Directory.class), anyString()))
                .thenThrow(new PictureFileException());

        //When
        try {
            instance.movePictureFile(
                    user.getLogin(),
                    buildPath(sourceDirectory.getPath(), filename),
                    buildPath(targetDirectory.getPath(), filename)
            );
        } catch (DriveException e) {
            //Then
            verify(fileRepository, never()).move(any(PictureFile.class), any(Directory.class), anyString());

            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void movePictureWhenPictureFileRepositoryThrowsExceptionOnMove() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .directory(sourceDirectory)
                .filename(filename)
                .data(filename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(sourceDirectory, filename))
                .thenReturn(singletonList(file));
        doThrow(new PictureFileException())
                .when(fileRepository).move(any(PictureFile.class), any(Directory.class), anyString());

        //When
        try {
            instance.movePictureFile(user.getLogin(), file.getPath(), buildPath(targetDirectory.getPath(), filename));
        } catch (DriveException e) {
            //Then
            verify(fileRepository).move(file, targetDirectory, filename);

            throw e;
        }
    }

    @Test
    public void deletePictureFileSuccess() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";
        PictureFile deletedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(singletonList(PictureFileBuilder.getInstance().from(deletedFile).build()));

        //When
        instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename));

        //Then
        verify(userInfoService).findUserByLogin(user.getLogin());
        verify(fileRepository).findByDirectoryAndFilename(directory, originalFilename);
        verify(fileRepository).delete(deletedFile);
    }


    @Test(expected = DriveException.class)
    public void deletePictureFileWhenDeletedFileNotFound() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(emptyList());

        //When
        try {
            instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename));
        } catch (DriveException e) {
            //Then
            verify(userInfoService).findUserByLogin(user.getLogin());
            verify(fileRepository).findByDirectoryAndFilename(directory, originalFilename);
            verify(fileRepository, never()).delete(any(PictureFile.class));

            throw e;
        }
    }


    @Test(expected = DriveException.class)
    public void deletePictureFileWhenFoundTooManyFiles() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";
        PictureFile deletedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(asList(
                        PictureFileBuilder.getInstance().from(deletedFile).build(),
                        PictureFileBuilder.getInstance().from(deletedFile).build()
                ));

        //When
        try {
            instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename));
        } catch (DriveException e) {
            //Then
            verify(fileRepository, never()).delete(any(PictureFile.class));

            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void deletePictureFileWhenPictureFileThrowsExceptionOnDelete() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";
        PictureFile deletedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(singletonList(PictureFileBuilder.getInstance().from(deletedFile).build()));
        doThrow(new PictureFileException()).when(fileRepository).delete(any(PictureFile.class));

        //When
        try {
            instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename));
        } catch (DriveException e) {
            //Then
            ArgumentCaptor<PictureFile> argumentCaptor = ArgumentCaptor.forClass(PictureFile.class);
            verify(fileRepository).delete(argumentCaptor.capture());
            assertThat(argumentCaptor.getValue())
                    .isNotNull()
                    .isEqualToIgnoringGivenFields(deletedFile, "id");

            throw e;
        }
    }

    @Test(expected = DriveException.class)
    public void deletePictureFileWhenPictureFileThrowsExceptionOnFind() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";

        when(userInfoService.findUserByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenThrow(new PictureFileException());

        //When
        try {
            instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename));
        } catch (DriveException e) {
            //Then
            verify(fileRepository).findByDirectoryAndFilename(directory, originalFilename);
            verify(fileRepository, never()).delete(any());

            throw e;
        }
    }
}