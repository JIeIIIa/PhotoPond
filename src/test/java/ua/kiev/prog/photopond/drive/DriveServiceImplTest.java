package ua.kiev.prog.photopond.drive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryRepository;
import ua.kiev.prog.photopond.drive.exception.DirectoryException;
import ua.kiev.prog.photopond.drive.exception.DriveException;
import ua.kiev.prog.photopond.drive.exception.PictureFileException;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.drive.DriveItemDTOMapper.toDTO;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application.properties")
@ActiveProfiles({"test"})
class DriveServiceImplTest {
    @MockBean
    private DirectoryRepository directoryRepository;

    @MockBean
    private PictureFileRepository fileRepository;

    @MockBean
    private UserInfoJpaRepository userInfoRepository;

    private DriveService instance;

    private UserInfo user;

    private Directory root;

    @BeforeEach
    void setUp() {
        reset(directoryRepository, fileRepository, userInfoRepository);
        instance = new DriveServiceImpl(directoryRepository, fileRepository, userInfoRepository);
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
        assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), sourcePath, targetPath)
        );

        //Then
        verify(directoryRepository, never()).move(any(Directory.class), any(Directory.class));
        verify(directoryRepository, never()).rename(any(Directory.class), anyString());
    }

    @Test
    void userHasNoDirectory() {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .from(root)
                .build();
        List<DriveItemDTO> expected = emptyList();

        when(directoryRepository.countByOwner(user)).thenReturn(0L);
        when(directoryRepository.findByOwnerAndPath(user, SEPARATOR))
                .thenReturn(emptyList())
                .thenReturn(emptyList())
                .thenReturn(singletonList(root));
        when(directoryRepository.save(any(Directory.class)))
                .thenAnswer(answer -> answer.getArguments()[0]);
        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        //When
        List<DriveItemDTO> result = instance.retrieveContent(user.getLogin(), SEPARATOR, true);

        //Then
        ArgumentCaptor<Directory> rootDirectory = ArgumentCaptor.forClass(Directory.class);
        verify(directoryRepository).save(rootDirectory.capture());
        assertThat(rootDirectory.getValue()).isEqualToIgnoringGivenFields(expectedRoot, "id");
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    void userHasNoDirectoryAndFailureCreating() {
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
        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        //When
        assertThrows(DriveException.class,
                () -> instance.retrieveContent(user.getLogin(), SEPARATOR, true)
        );

        //Then
        ArgumentCaptor<Directory> rootDirectory = ArgumentCaptor.forClass(Directory.class);
        verify(directoryRepository).save(rootDirectory.capture());
        assertThat(rootDirectory.getValue()).isEqualToIgnoringGivenFields(expectedRoot, "id");
    }

    @Test
    void rootDirectoryWithoutContent() {
        //Given
        when(directoryRepository.countByOwner(user)).thenReturn(1L);
        when(directoryRepository.findByOwnerAndPath(user, SEPARATOR)).thenReturn(singletonList(root));
        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));

        List<DriveItemDTO> expected = emptyList();

        //When
        List<DriveItemDTO> content = instance.retrieveContent(user.getLogin(), SEPARATOR, true);

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
        assertThat(content).hasSameElementsAs(expected);
    }

    @Test
    void rootDirectoryWithContent() {
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
        List<DriveItemDTO> expected = emptyList();

        when(directoryRepository.findTopLevelSubDirectories(new DirectoryBuilder().from(directories[2]).build()))
                .thenReturn(new LinkedList<>());
        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));

        //When
        List<DriveItemDTO> content = instance.retrieveContent(user.getLogin(), directories[2].getPath(), true);

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
        assertThat(content).hasSameElementsAs(expected);
    }

    @Test
    void notRootDirectoryWithContent() {
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
        List<DriveItemDTO> expected = singletonList(toDTO(new DirectoryBuilder().from(directories[2]).build()));

        when(directoryRepository.findTopLevelSubDirectories(new DirectoryBuilder().from(directories[1]).build()))
                .thenReturn(singletonList(new DirectoryBuilder().from(directories[2]).build()));
        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));

        //When
        List<DriveItemDTO> result = instance.retrieveContent(user.getLogin(), directories[1].getPath(), true);

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
        assertThat(result).hasSameElementsAs(expected);
    }

    @Nested
    class RetrieveDirectories {
        Directory[] directories;
        DirectoriesDTO expected;
        String baseUrl;

        @BeforeEach
        void setUp() {
            directories = new Directory[4];
            directories[0] = new DirectoryBuilder().owner(user).path(SEPARATOR).build();
            directories[1] = new DirectoryBuilder().owner(user).path(SEPARATOR + "first").build();
            directories[2] = new DirectoryBuilder().owner(user).path(directories[1].getPath() + SEPARATOR + "second").build();
            directories[3] = new DirectoryBuilder().owner(user).path(directories[1].getPath() + SEPARATOR + "third").build();
            baseUrl = "/api/" + user.getLogin() + "/directories";
            expected = new DirectoriesDTO();
            expected.setCurrent(toDTO(directories[1], baseUrl));
            expected.setParent(toDTO(directories[0], baseUrl));
            expected.setChildDirectories(asList(toDTO(directories[2], baseUrl), toDTO(directories[3], baseUrl)));

        }

        @Test
        void success() {
            //Given
            when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
            when(directoryRepository.findByOwnerAndPath(user, SEPARATOR + "first")).thenReturn(singletonList(directories[1]));
            when(directoryRepository.findTopLevelSubDirectories(directories[1])).thenReturn(asList(directories[2], directories[3]));

            //When
            DirectoriesDTO directoriesDTO = instance.retrieveDirectories(user.getLogin(), directories[1].getPath());

            //Then
            assertThat(directoriesDTO).isEqualToComparingFieldByFieldRecursively(expected);
            verify(userInfoRepository, times(1)).findByLogin(user.getLogin());
            verify(directoryRepository, times(1)).findByOwnerAndPath(user, SEPARATOR + "first");
            verify(directoryRepository, times(1)).findTopLevelSubDirectories(directories[1]);
            verifyNoMoreInteractions(userInfoRepository);
            verifyNoMoreInteractions(directoryRepository);
        }

        @Test
        void withException() {
            //Given
            when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
            when(directoryRepository.findByOwnerAndPath(user, SEPARATOR + "first")).thenReturn(singletonList(directories[1]));
            when(directoryRepository.findTopLevelSubDirectories(directories[1])).thenThrow(DataRetrievalFailureException.class);

            //When
            assertThrows(DriveException.class, () -> instance.retrieveDirectories(user.getLogin(), directories[1].getPath()));

            //Then
            verify(userInfoRepository, times(1)).findByLogin(user.getLogin());
            verify(directoryRepository, times(1)).findByOwnerAndPath(user, SEPARATOR + "first");
            verify(directoryRepository, times(1)).findTopLevelSubDirectories(directories[1]);
            verifyNoMoreInteractions(userInfoRepository);
            verifyNoMoreInteractions(directoryRepository);
        }
    }

    @Test
    void addDirectorySuccess() {
        //Given
        Directory expected = new DirectoryBuilder().path(buildPath(root.getPath(), "first")).owner(user).build();
        DriveItemDTO expectedDTO = toDTO(new DirectoryBuilder().path(buildPath(root.getPath(), "first")).owner(user).build());

        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
        when(directoryRepository.exists(user, expected.getPath())).thenReturn(false);
        when(directoryRepository.save(any(Directory.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        DriveItemDTO result = instance.addDirectory(user.getLogin(), root.getPath(), expected.getName());

        //Then
        verify(directoryRepository).save(expected);
        assertThat(result).isEqualToComparingFieldByField(expectedDTO);
    }

    @Test
    void addDirectoryWhenLoginFailure() {
        //Given
        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.empty());

        //When
        assertThrows(DriveException.class,
                () -> instance.addDirectory(user.getLogin(), root.getPath(), "/somePath")
        );

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
    }

    @Test
    void addDirectoryWhenDirectoryAlreadyExists() {
        //Given
        String newDirectoryPath = buildPath(root.getPath(), "existsDirectory");
        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.exists(user, newDirectoryPath)).thenReturn(true);

        //When
        assertThrows(DriveException.class,
                () -> instance.addDirectory(user.getLogin(), root.getPath(), newDirectoryPath)
        );

        //Then
        verify(directoryRepository, never()).save(any(Directory.class));
    }

    @Test
    void addDirectoryWhenDirectoryRepositoryThrowsException() {
        //Given
        Directory expected = new DirectoryBuilder().path(buildPath(root.getPath(), "first")).owner(user).build();

        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
        when(directoryRepository.exists(user, expected.getPath())).thenReturn(false);
        when(directoryRepository.save(any(Directory.class))).thenThrow(new DirectoryException());

        //When
        assertThrows(DriveException.class,
                () -> instance.addDirectory(user.getLogin(), root.getPath(), expected.getPath())
        );

        //Then
        verify(directoryRepository).save(expected);
    }

    @Test
    void moveDirectorySuccess() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();
        Directory expected = new DirectoryBuilder().owner(user).path(buildPath(target.getPath(), source.getName())).build();

        when(userInfoRepository.findByLogin(user.getLogin()))
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

    @Test
    void moveDirectoryWhenDirectoryRepositoryThrowsException() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();
        Directory expected = new DirectoryBuilder().owner(user).path(buildPath(target.getPath(), source.getName())).build();

        when(userInfoRepository.findByLogin(user.getLogin()))
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
        assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), source.getPath(), expected.getPath())
        );

        //Then
        verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
        verify(directoryRepository).findByOwnerAndPath(user, targetPath);
        verify(directoryRepository).move(source, target);
        verify(directoryRepository, never()).rename(any(Directory.class), anyString());
    }


    @Test
    void moveDirectoryWhenUserNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }

    @Test
    void moveDirectoryWhenSourceDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/first/second").build();
        String targetPath = target.getPath();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(emptyList());
        when(directoryRepository.findByOwnerAndPath(user, target.getPath()))
                .thenReturn(singletonList(target));

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }


    @Test
    void moveDirectoryWhenTargetDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory target = new DirectoryBuilder().owner(user).path("/another").build();
        String targetPath = target.getPath();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(singletonList(source));
        when(directoryRepository.findByOwnerAndPath(user, target.getPath()))
                .thenReturn(emptyList());

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }

    @Test
    void renameDirectorySuccess() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory parent = new DirectoryBuilder().owner(user).path("/first").build();
        String parentPath = parent.getPath();
        String targetPath = buildPath(parent.getPath(), "newName");
        Directory expected = new DirectoryBuilder().owner(user).path(targetPath).build();

        when(userInfoRepository.findByLogin(user.getLogin()))
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

    @Test
    void renameDirectoryWhenDirectoryRepositoryThrowsException() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory parent = new DirectoryBuilder().owner(user).path("/first").build();
        String parentPath = parent.getPath();
        String targetPath = buildPath(parent.getPath(), "newName");
        Directory expected = new DirectoryBuilder().owner(user).path(targetPath).build();

        when(userInfoRepository.findByLogin(user.getLogin()))
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
        assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), source.getPath(), expected.getPath())
        );

        //Then
        verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
        verify(directoryRepository).findByOwnerAndPath(user, parentPath);
        verify(directoryRepository).rename(source, targetPath);
        verify(directoryRepository, never()).move(any(Directory.class), any(Directory.class));
    }

    @Test
    void renameDirectoryWhenSourceDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();
        Directory parent = new DirectoryBuilder().owner(user).path("/first").build();
        String targetPath = buildPath(source.parentPath(), "another");

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, source.getPath()))
                .thenReturn(emptyList());
        when(directoryRepository.findByOwnerAndPath(user, parent.getPath()))
                .thenReturn(singletonList(parent));

        //When
        callMoveDirectoryWhenMockMoveAndRenameNotInvocation(sourcePath, targetPath);
    }

    @Test
    void deleteDirectorySuccess() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourcePath))
                .thenReturn(singletonList(source));

        //When
        instance.deleteDirectory(user.getLogin(), sourcePath);

        //Then
        verify(directoryRepository).findByOwnerAndPath(user, sourcePath);
        verify(directoryRepository).delete(source);
    }

    @Test
    void deleteDirectoryWhenOwnerNotExist() {
        //Given
        when(userInfoRepository.findByLogin(anyString()))
                .thenReturn(Optional.empty());

        //When
        assertThrows(DriveException.class,
                () -> instance.deleteDirectory(user.getLogin(), "/someDirectory")
        );

        //Then
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(directoryRepository, never()).findByOwnerAndPath(any(UserInfo.class), anyString());
        verify(directoryRepository, never()).delete(any(Directory.class));
    }

    @Test
    void deleteDirectoryWhenSourceDirectoryNotExists() {
        //Given
        Directory source = new DirectoryBuilder().owner(user).path("/first/second").build();
        String sourcePath = source.getPath();

        when(userInfoRepository.findByLogin(user.getLogin()))
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
    void retrievePictureFileDataSuccess() {
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


        when(userInfoRepository.findByLogin(user.getLogin()))
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
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(directoryRepository).findByOwnerAndPath(user, path);
        verify(fileRepository).findByDirectoryAndFilename(directory, filename);
    }

    @Test
    void retrievePictureFileDataWhenFoundTooManyFiles() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        String filePath = buildPath(directory.getPath(), filename);
        PictureFile pictureFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(filename)
                .build();


        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, filename))
                .thenReturn(asList(
                        PictureFileBuilder.getInstance().from(pictureFile).build(),
                        PictureFileBuilder.getInstance().from(pictureFile).build()
                ));

        //When
        assertThrows(DriveException.class,
                () -> instance.retrievePictureFileData(user.getLogin(), filePath)
        );
    }

    @Test
    void retrievePictureFileDataWhenFileNotFound() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        String filePath = buildPath(directory.getPath(), filename);

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, filename))
                .thenReturn(emptyList());

        //When
        assertThrows(DriveException.class,
                () -> instance.retrievePictureFileData(user.getLogin(), filePath)
        );
    }

    @Test
    void retrievePictureFileDataWhenFileRepositoryThrowsException() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/first/second").build();
        String path = directory.getPath();
        String filename = "awesomeFile.jpg";
        String filePath = buildPath(directory.getPath(), filename);

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, path))
                .thenReturn(singletonList(directory));
        doThrow(new PictureFileException()).when(fileRepository).findByDirectoryAndFilename(directory, filename);

        //When
        assertThrows(DriveException.class,
                () -> instance.retrievePictureFileData(user.getLogin(), filePath)
        );
    }

    @Test
    void addPictureFileSuccess() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/someDirectory").build();
        String originalFilename = "awesomeFile.jpg";
        MultipartFile file = new MockMultipartFile("filename", originalFilename, "image/jpeg", originalFilename.getBytes());

        PictureFile expected = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();
        DriveItemDTO expectedDTO = toDTO(expected);

        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath())).thenReturn(singletonList(directory));
        when(fileRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        //When
        DriveItemDTO result = instance.addPictureFile(user.getLogin(), directory.getPath(), file);

        //Then
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(fileRepository).save(expected);
        assertThat(result)
                .isNotNull()
                .isEqualToComparingFieldByField(expectedDTO);
    }

    @Test
    void addPictureFileWhenMultipartFileThrowsException() throws IOException {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/someDirectory").build();
        String originalFilename = "awesomeFile.jpg";

        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getBytes()).thenThrow(new IOException());

        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath())).thenReturn(singletonList(directory));

        //When
        assertThrows(DriveException.class,
                () -> instance.addPictureFile(user.getLogin(), directory.getPath(), file)
        );

        //Then
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(fileRepository, never()).save(any(PictureFile.class));
    }

    @Test
    void addPictureFileWhenPictureFileRepositoryThrowsException() throws IOException {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/someDirectory").build();
        String originalFilename = "awesomeFile.jpg";

        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getBytes()).thenReturn(originalFilename.getBytes());

        when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath())).thenReturn(singletonList(directory));
        when(fileRepository.save(any(PictureFile.class))).thenThrow(new PictureFileException());

        //When
        assertThrows(DriveException.class,
                () -> instance.addPictureFile(user.getLogin(), directory.getPath(), file)
        );

        //Then
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(fileRepository).save(any(PictureFile.class));
    }

    @Test
    void moveAndRenamePictureFileSuccess() {
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

        when(userInfoRepository.findByLogin(user.getLogin()))
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

    @Test
    void movePictureFileWhenFileNotFound() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(sourceDirectory, filename))
                .thenReturn(emptyList());

        //When
        assertThrows(DriveException.class,
                () -> instance.movePictureFile(
                        user.getLogin(),
                        buildPath(sourceDirectory.getPath(), filename),
                        buildPath(targetDirectory.getPath(), filename))
        );

        //Then
        verify(fileRepository, never()).move(any(PictureFile.class), any(Directory.class), anyString());
    }


    @Test
    void movePictureWhenFoundTooManyFiles() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .directory(sourceDirectory)
                .filename(filename)
                .data(filename.getBytes())
                .build();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(sourceDirectory, filename))
                .thenReturn(asList(file, file));

        //When
        assertThrows(DriveException.class,
                () -> instance.movePictureFile(user.getLogin(), file.getPath(), buildPath(targetDirectory.getPath(), filename))
        );

        //Then
        verify(fileRepository, never()).move(any(PictureFile.class), any(Directory.class), anyString());
    }

    @Test
    void movePictureWhenPictureFileRepositoryThrowsExceptionOnFind() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, sourceDirectory.getPath()))
                .thenReturn(singletonList(sourceDirectory));
        when(directoryRepository.findByOwnerAndPath(user, targetDirectory.getPath()))
                .thenReturn(singletonList(targetDirectory));
        when(fileRepository.findByDirectoryAndFilename(any(Directory.class), anyString()))
                .thenThrow(new PictureFileException());

        //When
        assertThrows(DriveException.class,
                () -> instance.movePictureFile(
                        user.getLogin(),
                        buildPath(sourceDirectory.getPath(), filename),
                        buildPath(targetDirectory.getPath(), filename))
        );

        //Then
        verify(fileRepository, never()).move(any(PictureFile.class), any(Directory.class), anyString());
    }

    @Test
    void movePictureWhenPictureFileRepositoryThrowsExceptionOnMove() {
        //Given
        Directory sourceDirectory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        Directory targetDirectory = new DirectoryBuilder().owner(user).path("/another").build();
        String filename = "awesomeFilename.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .directory(sourceDirectory)
                .filename(filename)
                .data(filename.getBytes())
                .build();

        when(userInfoRepository.findByLogin(user.getLogin()))
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
        assertThrows(DriveException.class,
                () -> instance.movePictureFile(user.getLogin(), file.getPath(), buildPath(targetDirectory.getPath(), filename))
        );

        //Then
        verify(fileRepository).move(file, targetDirectory, filename);
    }

    @Test
    void deletePictureFileSuccess() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";
        PictureFile deletedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(singletonList(PictureFileBuilder.getInstance().from(deletedFile).build()));

        //When
        instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename));

        //Then
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(fileRepository).findByDirectoryAndFilename(directory, originalFilename);
        verify(fileRepository).delete(deletedFile);
    }


    @Test
    void deletePictureFileWhenDeletedFileNotFound() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(emptyList());

        //When
        assertThrows(DriveException.class,
                () -> instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename))
        );

        //Then
        verify(userInfoRepository).findByLogin(user.getLogin());
        verify(fileRepository).findByDirectoryAndFilename(directory, originalFilename);
        verify(fileRepository, never()).delete(any(PictureFile.class));
    }


    @Test
    void deletePictureFileWhenFoundTooManyFiles() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";
        PictureFile deletedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(asList(
                        PictureFileBuilder.getInstance().from(deletedFile).build(),
                        PictureFileBuilder.getInstance().from(deletedFile).build()
                ));

        //When
        assertThrows(DriveException.class,
                () -> instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename))
        );

        //Then
        verify(fileRepository, never()).delete(any(PictureFile.class));
    }

    @Test
    void deletePictureFileWhenPictureFileThrowsExceptionOnDelete() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";
        PictureFile deletedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenReturn(singletonList(PictureFileBuilder.getInstance().from(deletedFile).build()));
        doThrow(new PictureFileException()).when(fileRepository).delete(any(PictureFile.class));

        //When
        assertThrows(DriveException.class,
                () -> instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename))
        );

        //Then
        ArgumentCaptor<PictureFile> argumentCaptor = ArgumentCaptor.forClass(PictureFile.class);
        verify(fileRepository).delete(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue())
                .isNotNull()
                .isEqualToIgnoringGivenFields(deletedFile, "id");
    }

    @Test
    void deletePictureFileWhenPictureFileThrowsExceptionOnFind() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/some/Directory").build();
        String originalFilename = "awesomeFile.jpg";

        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.ofNullable(user));
        when(directoryRepository.findByOwnerAndPath(user, directory.getPath()))
                .thenReturn(singletonList(directory));
        when(fileRepository.findByDirectoryAndFilename(directory, originalFilename))
                .thenThrow(new PictureFileException());

        //When
        assertThrows(DriveException.class,
                () -> instance.deletePictureFile(user.getLogin(), buildPath(directory.getPath(), originalFilename))
        );

        //Then
        verify(fileRepository).findByDirectoryAndFilename(directory, originalFilename);
        verify(fileRepository, never()).delete(any());
    }

    @Nested
    class MakeStatistics {
        @Test
        void success() {
            //Given
            when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.ofNullable(user));
            when(directoryRepository.findByOwnerAndPathStartingWith(user, SEPARATOR)).thenReturn(singletonList(root));
            when(fileRepository.findByDirectory(root)).thenReturn(asList(
                    PictureFileBuilder.getInstance().filename("first").directory(root).data(new byte[]{1, 2, 3, 4, 5, 6, 7}).build(),
                    PictureFileBuilder.getInstance().filename("second").directory(root).data(new byte[]{8, 9, 10, 11}).build()
            ));
            when(fileRepository.pictureSize(any())).thenAnswer(
                    invocationOnMock -> (long) ((PictureFile) invocationOnMock.getArguments()[0]).getFilename().length()
            );
            DriveStatisticsDTO expected = new DriveStatisticsDTO(user.getLogin());
            expected.setDirectoriesSize(11L);
            expected.setPictureCount(2L);

            //When
            DriveStatisticsDTO statistics = instance.makeStatistics(user.getLogin());

            //Then
            assertThat(statistics).isEqualToComparingFieldByField(expected);
            verify(userInfoRepository, times(1)).findByLogin(user.getLogin());
            verify(directoryRepository, times(1)).findByOwnerAndPathStartingWith(user, SEPARATOR);
            verify(fileRepository, times(1)).findByDirectory(root);
            verify(fileRepository, times(2)).pictureSize(any());
            verifyNoMoreInteractions(userInfoRepository, directoryRepository, fileRepository);
        }

        @Test
        void userNotFound() {
            //Given
            when(userInfoRepository.findByLogin(user.getLogin())).thenReturn(Optional.empty());

            //When
            assertThrows(DriveException.class, () -> instance.makeStatistics(user.getLogin()));

            //Then
            verify(userInfoRepository, times(1)).findByLogin(user.getLogin());
            verifyNoMoreInteractions(userInfoRepository, directoryRepository, fileRepository);
        }
    }
}