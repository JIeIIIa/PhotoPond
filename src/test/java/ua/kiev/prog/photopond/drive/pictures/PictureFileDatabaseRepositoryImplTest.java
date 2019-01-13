package ua.kiev.prog.photopond.drive.pictures;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.exception.PictureFileException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;

@ExtendWith(SpringExtension.class)
@TestPropertySource({"classpath:application.properties"})
@ActiveProfiles({DATABASE_STORAGE, "test"})
class PictureFileDatabaseRepositoryImplTest {

    private static final Logger LOG = LogManager.getLogger(PictureFileDatabaseRepositoryImplTest.class);

    @MockBean
    private PictureFileJpaRepository pictureFileJpaRepository;

    @MockBean
    private PictureFileDataJpaRepository pictureFileDataJpaRepository;

    private PictureFileDatabaseRepositoryImpl instance;

    private UserInfo user;
    private Directory directory;
    private PictureFile pictureFile;
    private PictureFileData pictureFileData;
    private final byte[] DATA = {1, 2, 3, 4, 5, 6, 7};

    @BeforeEach
    void setUp() {
        instance = new PictureFileDatabaseRepositoryImpl(pictureFileJpaRepository, pictureFileDataJpaRepository);

        user = new UserInfoBuilder().id(7L).login("awesomeUser").role(UserRole.USER).build();

        directory = new DirectoryBuilder().id(777L).owner(user).path("/first").build();

        pictureFile = PictureFileBuilder.getInstance()
                .id(123L)
                .directory(directory)
                .data(DATA)
                .filename("pictureFile.jpg")
                .build();
        pictureFileData = new PictureFileData(pictureFile, new byte[]{1, 2, 3, 4, 5, 6, 7});

        when(pictureFileDataJpaRepository.save(any(PictureFileData.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    @Test
    void saveNullPictureFile() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.save(null)
        );
    }

    @Test
    void saveWhenPictureFileNotExists() {
        //Given
        when(pictureFileJpaRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );


        //When
        PictureFile result = instance.save(pictureFile);

        //Then
        verify(pictureFileJpaRepository).save(pictureFile);
        verify(pictureFileDataJpaRepository).save(refEq(pictureFileData, "id"));
        assertThat(result).isEqualToIgnoringGivenFields(pictureFile, "id");
    }

    @Test
    void saveFileWithNullData() {
        //Given
        when(pictureFileJpaRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );
        pictureFile.setData(null);

        //When
        assertThrows(PictureFileException.class,
                () -> instance.save(pictureFile)
        );
    }

    @Test
    void saveWhenPictureFileExistsInDatabase() {
        //Given
        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename()))
                .thenReturn(Optional.ofNullable(pictureFile));

        //When
        assertThrows(PictureFileException.class,
                () -> instance.save(pictureFile)
        );

        //Then
        verify(pictureFileJpaRepository, never()).save(any(PictureFile.class));
        verify(pictureFileDataJpaRepository, never()).save(any(PictureFileData.class));
        verify(pictureFileJpaRepository).findFirstByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename());
    }

    @Test
    void saveSuccess() {
        //Given
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();
        when(pictureFileJpaRepository.save(any(PictureFile.class)))
                .thenAnswer((Answer<PictureFile>) mockInvocation -> (PictureFile) mockInvocation.getArguments()[0]);

        //When
        PictureFile result = instance.save(pictureFile);

        //Then
        assertThat(result).isEqualToComparingFieldByField(expected);
        verify(pictureFileDataJpaRepository).save(refEq(pictureFileData, "id"));
    }

    @Test
    void deleteNullPictureFile() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.delete(null)
        );
    }

    @Test
    void deleteSuccess() {
        //When
        instance.delete(pictureFile);

        //Then
        verify(pictureFileJpaRepository).delete(pictureFile);
    }

    @Test
    void deleteWithJpaRepositoryException() {
        //Given
        doThrow(QueryTimeoutException.class).when(pictureFileJpaRepository).delete(pictureFile);

        //When
        assertThrows(PictureFileException.class,
                () -> instance.delete(pictureFile)
        );

        //Then
        verify(pictureFileJpaRepository).delete(pictureFile);
    }

    @Test
    void moveTargetEqualsSource() {
        //Given
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();

        //When
        instance.move(pictureFile, pictureFile.getDirectory(), pictureFile.getFilename());

        //Then
        verify(pictureFileJpaRepository, never()).save(any(PictureFile.class));
        verify(pictureFileDataJpaRepository, never()).save(any(PictureFileData.class));
        verify(pictureFileJpaRepository, never()).findByDirectoryAndFilename(any(Directory.class), any(String.class));
        verify(pictureFileJpaRepository, never()).findFirstByDirectoryAndFilename(any(Directory.class), any(String.class));
        assertThat(pictureFile).isEqualToComparingFieldByField(expected);
    }

    @Test
    void moveWhenTargetFileAlreadyExistsInDatabase() {
        //Given
        String targetFilename = "targetFilename.jpg";
        final byte[] SOME_DATA = {7, 8, 7, 8, 7};
        PictureFile anotherFile = PictureFileBuilder.getInstance()
                .id(2L)
                .directory(directory)
                .filename(targetFilename)
                .data(SOME_DATA)
                .build();

        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(anotherFile.getDirectory(), anotherFile.getFilename()))
                .thenReturn(Optional.of(anotherFile));

        //When
        assertThrows(PictureFileException.class,
                () -> instance.move(pictureFile, directory, targetFilename)
        );
    }

    @Test
    void moveWhenWrongTargetFileName() {
        //Given
        String targetFilename = "/wrong/file.name";

        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, targetFilename))
                .thenReturn(Optional.empty());

        //When
        assertThrows(PictureFileException.class,
                () -> instance.move(pictureFile, directory, targetFilename)
        );

        //Then
        verify(pictureFileJpaRepository, never()).save(any(PictureFile.class));
        verify(pictureFileDataJpaRepository, never()).save(any(PictureFileData.class));
    }

    @Test
    void moveBetweenDifferentUsers() {
        //Given
        UserInfo anotherUser = new UserInfoBuilder().id(5L).login("anotherUser").password("qwerty123!").build();
        Directory anotherDirectory = new DirectoryBuilder().id(9L).owner(anotherUser).path("/somewhere").build();

        //When
        assertThrows(PictureFileException.class,
                () -> instance.move(pictureFile, anotherDirectory, "newName")
        );

        //Then
        verify(pictureFileJpaRepository, never()).findFirstByDirectoryAndFilename(any(), any());
        verify(pictureFileJpaRepository, never()).findByDirectoryAndFilename(any(), any());
        verify(pictureFileJpaRepository, never()).save(any());
        verify(pictureFileDataJpaRepository, never()).save(any());
        assertThat(pictureFile.getDirectory().getOwner()).isEqualTo(user);
        assertThat(pictureFile.getData()).isEqualTo(DATA);
    }

    @Test
    void moveWhenPictureFileThrowsException() {
        //Given
        Directory targetDirectory = new DirectoryBuilder().id(3L).owner(user).path("/second").build();
        String targetFilename = "targetFilename.jpg";
        PictureFile expectedFile = PictureFileBuilder.getInstance()
                .id(pictureFile.getId())
                .directory(targetDirectory)
                .filename(targetFilename)
                .data(DATA)
                .build();
        pictureFile = spy(pictureFile);

        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(expectedFile.getDirectory(), expectedFile.getFilename()))
                .thenReturn(Optional.empty());
        doThrow(IllegalArgumentException.class).when(pictureFile).setFilename(any());

        //When
        assertThrows(PictureFileException.class, () -> instance.move(pictureFile, targetDirectory, targetFilename));
    }

    @Test
    void moveToAnotherDirectorySuccess() {
        //Given
        Directory targetDirectory = new DirectoryBuilder().id(3L).owner(user).path("/second").build();
        String targetFilename = "targetFilename.jpg";
        PictureFile expectedFile = PictureFileBuilder.getInstance()
                .id(pictureFile.getId())
                .directory(targetDirectory)
                .filename(targetFilename)
                .data(DATA)
                .build();

        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(expectedFile.getDirectory(), expectedFile.getFilename()))
                .thenReturn(Optional.empty());

        //When
        instance.move(pictureFile, targetDirectory, targetFilename);

        //Then
        assertThat(pictureFile).isEqualToComparingFieldByField(expectedFile);
    }

    @Test
    void pictureSizeWhenFileNotExists() {
        //Given
        when(pictureFileDataJpaRepository.findByPictureFile(pictureFile)).thenReturn(Optional.empty());

        //When
        long size = instance.pictureSize(pictureFile);

        //Then
        assertThat(size).isEqualTo(0);
    }

    @Test
    void pictureSizeSuccess() {
        //Given
        when(pictureFileDataJpaRepository.findByPictureFile(pictureFile)).thenReturn(Optional.of(pictureFileData));

        //When
        long size = instance.pictureSize(pictureFile);

        //Then
        assertThat(size).isEqualTo(DATA.length);
    }

    @Test
    void findByIdSuccess() {
        //Given
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();
        when(pictureFileJpaRepository.findById(pictureFile.getId()))
                .thenReturn(Optional.ofNullable(pictureFile));
        when(pictureFileDataJpaRepository.findByPictureFile(pictureFile))
                .thenReturn(Optional.of(pictureFileData));

        //When
        Optional<PictureFile> result = instance.findById(pictureFile.getId());

        //Then
        verify(pictureFileJpaRepository).findById(expected.getId());
        assertThat(result)
                .isNotNull()
                .isPresent().get()
                .isEqualToComparingFieldByField(expected);
    }

    @Test
    void findByIdFailure() {
        //Given
        Long id = 5L;
        when(pictureFileJpaRepository.findById(id)).thenReturn(Optional.empty());

        //When
        Optional<PictureFile> result = instance.findById(id);

        //Then
        verify(pictureFileJpaRepository).findById(id);
        assertThat(result)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    void findByDirectorySuccess() {
        //Given
        final byte[] OTHER_DATA = {7, 8, 7, 8, 7};
        PictureFile otherFile = PictureFileBuilder.getInstance()
                .id(124L).directory(directory).filename("someName.jpg").data(OTHER_DATA)
                .build();
        PictureFileData otherFileData = new PictureFileData(otherFile, OTHER_DATA);
        List<PictureFile> expected = new LinkedList<>();
        expected.add(PictureFileBuilder.getInstance().from(pictureFile).build());
        expected.add(PictureFileBuilder.getInstance().from(otherFile).build());


        when(pictureFileJpaRepository.findByDirectory(directory))
                .thenReturn(Arrays.asList(pictureFile, otherFile));
        when(pictureFileDataJpaRepository.findByPictureFile(pictureFile))
                .thenReturn(Optional.of(pictureFileData));
        when(pictureFileDataJpaRepository.findByPictureFile(otherFile))
                .thenReturn(Optional.of(otherFileData));


        //When
        List<PictureFile> result = instance.findByDirectory(directory);

        //Then
        verify(pictureFileJpaRepository).findByDirectory(directory);
        assertThat(result)
                .isNotNull()
                .hasSameSizeAs(expected)
                .containsExactlyInAnyOrder(expected.toArray(new PictureFile[0]));
    }

    @Test
    void findByDirectoryFailure() {
        //Given
        when(pictureFileJpaRepository.findByDirectory(directory)).thenReturn(emptyList());

        //When
        List<PictureFile> result = instance.findByDirectory(directory);

        //Then
        verify(pictureFileJpaRepository).findByDirectory(directory);
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void findByDirectoryAndFilenameSuccess() {
        //Given
        List<PictureFile> expected = new LinkedList<>();
        PictureFile expectedFile = PictureFileBuilder.getInstance().from(pictureFile).build();
        expected.add(expectedFile);


        when(pictureFileJpaRepository.findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename()))
                .thenReturn(singletonList(pictureFile));
        when(pictureFileDataJpaRepository.findByPictureFile(pictureFile))
                .thenReturn(Optional.of(pictureFileData));

        //When
        List<PictureFile> result = instance.findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename());

        //Then
        verify(pictureFileJpaRepository).findByDirectoryAndFilename(expectedFile.getDirectory(), expectedFile.getFilename());
        assertThat(result)
                .isNotNull()
                .hasSameSizeAs(expected)
                .containsExactlyInAnyOrder(expected.toArray(new PictureFile[0]));
    }

    @Test
    void findByDirectoryAndFileNameFailure() {
        //Given
        when(pictureFileJpaRepository.findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename()))
                .thenReturn(emptyList());

        //When
        List<PictureFile> result = instance.findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename());

        //Then
        verify(pictureFileJpaRepository).findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename());
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }
}