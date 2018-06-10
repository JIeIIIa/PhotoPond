package ua.kiev.prog.photopond.drive.pictures;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
public class PictureFileDiskAndDatabaseRepositoryImplTest {
    @MockBean
    private PictureFileJpaRepository pictureFileJpaRepository;

    private PictureFileDiskAndDatabaseRepositoryImpl instance;

    @Value(value = "${folders.basedir.location}")
    private String foldersBasedir;

    private Path basedirPath;
    private UserInfo user;
    private Directory directory;
    private Path directoryPathOnDisk;
    private PictureFile pictureFile;
    private Path pictureFileOnDisk;
    private final byte[] DATA = {1, 2, 3, 4, 5, 6, 7};

    @Before
    public void setUp() throws Exception {
        basedirPath = Paths.get(foldersBasedir);

        instance = new PictureFileDiskAndDatabaseRepositoryImpl(pictureFileJpaRepository);
        instance.setFoldersBasedir(foldersBasedir);

        FileUtils.cleanDirectory(basedirPath.toFile());

        user = new UserInfoBuilder().id(7L).login("awesomeUser").role(UserRole.USER).build();

        directory = new DirectoryBuilder().id(777L).owner(user).path("/first").build();
        directoryPathOnDisk = Paths.get(basedirPath + directory.getFullPath());
        Files.createDirectories(directoryPathOnDisk);

        pictureFile = PictureFileBuilder.getInstance()
                .id(123L)
                .directory(directory)
                .data(DATA)
                .filename("pictureFile.jpg")
                .build();
        pictureFileOnDisk = Paths.get(basedirPath + pictureFile.getFullPath());
        Files.write(pictureFileOnDisk, DATA);
    }

    private void assertThatFileContainsData(PictureFile pictureFile, byte[] data) throws IOException {
        Path path = Paths.get(basedirPath + pictureFile.getFullPath());
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.readAllBytes(path)).isEqualTo(data);
    }

    @Test
    public void isBasedirExists() {
        //Then
        assertThat(Files.exists(basedirPath)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNullPictureFile() {
        //When
        instance.save(null);
    }

    @Test
    public void saveWhenPictureFileNotExists() throws IOException {
        //Given
        when(pictureFileJpaRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );
        FileUtils.deleteQuietly(pictureFileOnDisk.toFile());

        //When
        PictureFile result = instance.save(pictureFile);

        //Then
        verify(pictureFileJpaRepository).save(pictureFile);
        assertThatFileContainsData(pictureFile, DATA);
        assertThat(result).isEqualToIgnoringGivenFields(pictureFile, "id");
    }

    @Test(expected = PictureFileException.class)
    public void saveFileWithNullData() {
        //Given
        when(pictureFileJpaRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );
        pictureFile.setData(null);
        FileUtils.deleteQuietly(pictureFileOnDisk.toFile());

        //When
        instance.save(pictureFile);
    }

    @Test
    public void saveWhenPictureFileExistsOnDisk() throws IOException {
        //Given
        when(pictureFileJpaRepository.save(any(PictureFile.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );
        FileUtils.deleteQuietly(pictureFileOnDisk.toFile());
        Files.write(pictureFileOnDisk, new byte[]{1, 2, 3});

        //When
        instance.save(pictureFile);

        //Then
        verify(pictureFileJpaRepository).save(pictureFile);
        assertThatFileContainsData(pictureFile, DATA);
    }

    @Test(expected = PictureFileException.class)
    public void saveWhenPictureFileExistsInDatabase() {
        //Given
        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename()))
                .thenReturn(java.util.Optional.ofNullable(pictureFile));

        //When
        try {
            instance.save(pictureFile);
        } catch (PictureFileException e) {
            //Then
            verify(pictureFileJpaRepository, never()).save(any(PictureFile.class));
            verify(pictureFileJpaRepository).findFirstByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename());
            throw e;
        }
    }

    @Test
    public void saveSuccess() throws IOException {
        //Given
        FileUtils.deleteQuietly(pictureFileOnDisk.toFile());
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();
        when(pictureFileJpaRepository.save(any(PictureFile.class)))
                .thenAnswer((Answer<PictureFile>) mockInvocation -> (PictureFile) mockInvocation.getArguments()[0]);

        //When
        PictureFile result = instance.save(pictureFile);

        //Then
        assertThat(result).isEqualToComparingFieldByField(expected);
        assertThatFileContainsData(result, DATA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullPictureFile() {
        //When
        instance.delete(null);
    }

    @Test
    public void deleteSuccess() {
        //When
        instance.delete(pictureFile);

        //Then
        verify(pictureFileJpaRepository).delete(pictureFile);
        assertThat(Files.exists(pictureFileOnDisk)).isFalse();
    }

    @Test(expected = PictureFileException.class)
    public void deleteWithJpaRepositoryException() throws IOException {
        //Given
        Files.write(pictureFileOnDisk, new byte[]{1, 2, 3});
        doThrow(QueryTimeoutException.class).when(pictureFileJpaRepository).delete(pictureFile);

        try {
            //When
            instance.delete(pictureFile);
        } catch (PictureFileException e) {
            //Then
            verify(pictureFileJpaRepository).delete(pictureFile);
            throw e;
        }
    }

    @Test
    public void deleteWhenFileExistsOnlyInDatabase() {
        //Given
        FileUtils.deleteQuietly(pictureFileOnDisk.toFile());

        //When
        instance.delete(pictureFile);

        //Then
        verify(pictureFileJpaRepository).delete(pictureFile);
        assertThat(Files.exists(pictureFileOnDisk)).isFalse();
    }

    @Test
    public void moveTargetEqualsSource() throws IOException {
        //Given
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();

        //When
        instance.move(pictureFile, pictureFile.getDirectory(), pictureFile.getFilename());

        //Then
        verify(pictureFileJpaRepository, never()).save(any(PictureFile.class));
        verify(pictureFileJpaRepository, never()).findByDirectoryAndFilename(any(Directory.class), any(String.class));
        verify(pictureFileJpaRepository, never()).findFirstByDirectoryAndFilename(any(Directory.class), any(String.class));
        assertThat(pictureFile).isEqualToComparingFieldByField(expected);
        assertThatFileContainsData(pictureFile, DATA);
    }

    @Test(expected = PictureFileException.class)
    public void moveWhenTargetFileAlreadyExistsInDatabase() throws Exception {
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
        try {
            instance.move(pictureFile, directory, targetFilename);
        } catch (PictureFileException e) {
            //Then
            assertThatFileContainsData(pictureFile, DATA);
            throw e;
        }
    }

    @Test
    public void moveWhenTargetFileAlreadyExistsOnDisk() throws Exception {
        //Given
        String targetFilename = "targetFilename.jpg";
        final byte[] SOME_DATA = {7, 8, 7, 8, 7};
        PictureFile anotherFile = PictureFileBuilder.getInstance()
                .id(2L)
                .directory(directory)
                .filename(targetFilename)
                .data(SOME_DATA)
                .build();
        Files.write(Paths.get(basedirPath + anotherFile.getFullPath()), SOME_DATA);

        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(anotherFile.getDirectory(), anotherFile.getFilename()))
                .thenReturn(Optional.empty());

        //When
        instance.move(pictureFile, directory, targetFilename);

        //Then
        assertThatFileContainsData(pictureFile, DATA);
        assertThat(pictureFile.getFilename()).isEqualTo(targetFilename);
        assertThat(pictureFile.getDirectory()).isEqualTo(directory);
        assertThat(Files.exists(pictureFileOnDisk)).isFalse();
    }

    @Test(expected = PictureFileException.class)
    public void moveWhenWrongTargetFileName() {
        //Given
        String targetFilename = "/wrong/file.name";

        when(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, targetFilename))
                .thenReturn(Optional.empty());

        //When
        try {
            instance.move(pictureFile, directory, targetFilename);
        } catch (PictureFileException e) {
            //Then
            verify(pictureFileJpaRepository, never()).save(any(PictureFile.class));
            throw e;
        }
    }

    @Test(expected = PictureFileException.class)
    public void moveWhenSourceFileNotExistsOnDisk() {
        //Given
        FileUtils.deleteQuietly(directoryPathOnDisk.toFile());

        //When
        try {
            instance.move(pictureFile, directory, "newFileName.jpg");
        } catch (PictureFileException e) {
            //Then
            verify(pictureFileJpaRepository, never()).findFirstByDirectoryAndFilename(any(), any());
            verify(pictureFileJpaRepository, never()).findByDirectoryAndFilename(any(), any());
            throw e;
        }
    }

    @Test(expected = PictureFileException.class)
    public void moveBetweenDifferentUsers() {
        //Given
        UserInfo anotherUser = new UserInfoBuilder().id(5).login("anotherUser").password("qwerty123!").build();
        Directory anotherDirectory = new DirectoryBuilder().id(9L).owner(anotherUser).path("/somewhere").build();

        //When
        try {
            instance.move(pictureFile, anotherDirectory, "newName");
        } catch (PictureFileException e) {
            //Then
            verify(pictureFileJpaRepository, never()).findFirstByDirectoryAndFilename(any(), any());
            verify(pictureFileJpaRepository, never()).findByDirectoryAndFilename(any(), any());
            verify(pictureFileJpaRepository, never()).save(any());
            assertThat(pictureFile.getDirectory().getOwner()).isEqualTo(user);
            assertThat(pictureFile.getData()).isEqualTo(DATA);
            throw e;
        }
    }

    @Test
    public void moveToAnotherDirectorySuccess() throws IOException {
        //Given
        Directory targetDirectory = new DirectoryBuilder().id(3L).owner(user).path("/second").build();
        Files.createDirectories(Paths.get(basedirPath + targetDirectory.getFullPath()));
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
        assertThatFileContainsData(pictureFile, DATA);
        assertThat(pictureFile).isEqualToComparingFieldByField(expectedFile);
    }

    @Test
    public void findByIdSuccess() {
        //Given
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();
        when(pictureFileJpaRepository.findById(pictureFile.getId()))
                .thenReturn(Optional.ofNullable(pictureFile));

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
    public void findByIdFailure() {
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
    public void findByDirectorySuccess() throws IOException {
        //Given
        final byte[] OTHER_DATA = {7, 8, 7, 8, 7};
        PictureFile otherFile = PictureFileBuilder.getInstance()
                .id(124L).directory(directory).filename("someName.jpg").data(OTHER_DATA)
                .build();
        Files.write(Paths.get(basedirPath + otherFile.getFullPath()), OTHER_DATA);
        List<PictureFile> expected = new LinkedList<>();
        expected.add(PictureFileBuilder.getInstance().from(pictureFile).build());
        expected.add(PictureFileBuilder.getInstance().from(otherFile).build());


        when(pictureFileJpaRepository.findByDirectory(directory))
                .thenReturn(Arrays.asList(pictureFile, otherFile));

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
    public void findByDirectoryFailure() {
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
    public void findByDirectoryAndFilenameSuccess() {
        //Given
        List<PictureFile> expected = new LinkedList<>();
        PictureFile expectedFile = PictureFileBuilder.getInstance().from(pictureFile).build();
        expected.add(expectedFile);


        when(pictureFileJpaRepository.findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename()))
                .thenReturn(singletonList(pictureFile));

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
    public void findByDirectoryAndFileNameFailure() {
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