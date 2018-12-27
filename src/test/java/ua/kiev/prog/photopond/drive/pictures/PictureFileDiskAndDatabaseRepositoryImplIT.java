package ua.kiev.prog.photopond.drive.pictures;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import ua.kiev.prog.photopond.Utils.TestUtils;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryJpaRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "test"})
@DataJpaTest
@EnableJpaAuditing
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:datasets/picturefile_dataset_IT.xml")
@Transactional
public class PictureFileDiskAndDatabaseRepositoryImplIT {

    private static final Logger LOG = LogManager.getLogger(PictureFileDiskAndDatabaseRepositoryImplIT.class);

    @Value(value = "${folders.basedir.location}")
    private String foldersBasedir;

    @Autowired
    private DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    private UserInfoJpaRepository userInfoJpaRepository;

    @Autowired
    private PictureFileJpaRepository pictureFileJpaRepository;

    private PictureFileDiskAndDatabaseRepositoryImpl instance;

    private Path basedirPath;
    private UserInfo user;
    private Directory directory;
    private PictureFile pictureFile;

    @BeforeEach
    public void setUp() throws IOException {
        basedirPath = Paths.get(foldersBasedir + "/" + ThreadLocalRandom.current().nextInt());

        instance = new PictureFileDiskAndDatabaseRepositoryImpl(pictureFileJpaRepository);
        instance.setFoldersBasedir(basedirPath.toString());

        TestUtils.createDirectories(basedirPath, directoryJpaRepository);
        TestUtils.createPictureFiles(basedirPath, pictureFileJpaRepository);

        user = userInfoJpaRepository.findByLogin("User")
                .orElseThrow(() -> new IllegalStateException("Failure retrieve User"));
        directory = directoryJpaRepository.findById(2000L)
                .orElseThrow(() -> new IllegalStateException("Failure retrieve Directory"));
        pictureFile = PictureFileBuilder.getInstance()
                .id(200L)
                .filename("root.jpg")
                .data("root.jpg".getBytes())
                .directory(directory)
                .build();
    }

    @AfterEach
    void tearDown() {
        try {
            FileUtils.deleteDirectory(basedirPath.toFile());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
    }

    private void assertThatFileContainsData(PictureFile pictureFile, byte[] data) throws IOException {
        Path path = Paths.get(basedirPath + pictureFile.getFullPath());
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.readAllBytes(path)).isEqualTo(data);
    }

    @Test
    public void isBasedirExists() {
        //Then
        Assertions.assertThat(Files.exists(basedirPath)).isTrue();
    }

    @Test
    public void saveNullPictureFile() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.save(null)
        );
    }

    @Test
    public void saveWhenPictureFileNotExists() throws IOException {
        //Given
        Date start = new Date();
        String filename = "anotherFile.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .filename(filename)
                .directory(directory)
                .data(filename.getBytes())
                .build();
        PictureFile expected = PictureFileBuilder.getInstance().from(file).build();

        //When
        PictureFile result = instance.save(file);

        //Then
        assertThatFileContainsData(expected, filename.getBytes());
        assertThat(result).isEqualToIgnoringGivenFields(expected, "id", "creationDate");
        Calendar finish = Calendar.getInstance();
        finish.add(Calendar.SECOND, 1);
        assertThat(result.getCreationDate()).isBetween(start, finish.getTime());

    }

    @Test
    public void saveFileWithNullData() {
        //Given
        String filename = "anotherFile.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .filename(filename)
                .directory(directory)
                .build();
        file.setData(null);

        //When
        assertThrows(PictureFileException.class,
                () -> instance.save(file)
        );

        //Then
        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, filename))
                .isNotPresent();
        assertThat(Files.exists(Paths.get(basedirPath + directory.getFullPath() + "/" + filename))).isFalse();
    }

    @Test
    public void saveWhenPictureFileExistsOnDisk() throws IOException {
        //Given
        Date start = new Date();
        pictureFileJpaRepository.delete(pictureFile);
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();

        //When
        PictureFile result = instance.save(pictureFile);

        //Then
        assertThatFileContainsData(result, expected.getData());
        assertThat(result).isEqualToIgnoringGivenFields(expected, "id", "creationDate");
        Calendar finish = Calendar.getInstance();
        finish.add(Calendar.SECOND, 1);
        assertThat(result.getCreationDate()).isBetween(start, finish.getTime());

    }

    @Test
    public void saveWhenPictureFileExistsInDatabase() {
        //When
        assertThrows(PictureFileException.class,
                () -> instance.save(pictureFile)
        );
    }

    @Test
    public void saveSuccess() throws IOException {
        //Given
        Date start = new Date();
        String filename = "anotherFile.jpg";
        PictureFile file = PictureFileBuilder.getInstance()
                .filename(filename)
                .directory(directory)
                .data(filename.getBytes())
                .build();
        PictureFile expected = PictureFileBuilder.getInstance().from(file).build();


        //When
        PictureFile result = instance.save(file);

        //Then
        assertThatFileContainsData(result, filename.getBytes());
        assertThat(result).isEqualToIgnoringGivenFields(expected, "id", "creationDate");
        Calendar finish = Calendar.getInstance();
        finish.add(Calendar.SECOND, 1);
        assertThat(result.getCreationDate()).isBetween(start, finish.getTime());

        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, filename))
                .isPresent();
    }


    @Test
    public void deleteNullPictureFile() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.delete(null)
        );
    }


    @Test
    public void deleteSuccess() {
        //Given
        Path pictureFileOnDisk = Paths.get(basedirPath + pictureFile.getFullPath());
        String filename = pictureFile.getFilename();

        //When
        instance.delete(pictureFile);

        //Then
        assertThat(Files.exists(pictureFileOnDisk)).isFalse();
        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, filename))
                .isNotPresent();
    }

    @Test
    public void deleteWhenFileExistsOnlyInDatabase() {
        //Given
        Path pictureFileOnDisk = Paths.get(basedirPath + pictureFile.getFullPath());
        String filename = pictureFile.getFilename();
        FileUtils.deleteQuietly(pictureFileOnDisk.toFile());

        //When
        instance.delete(pictureFile);

        //Then
        assertThat(Files.exists(pictureFileOnDisk)).isFalse();
        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, filename))
                .isNotPresent();
    }

    @Test
    public void moveTargetEqualsSource() throws IOException {
        //Given
        final byte[] DATA = pictureFile.getFilename().getBytes();
        String filename = pictureFile.getFilename();
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();

        //When
        instance.move(pictureFile, pictureFile.getDirectory(), pictureFile.getFilename());

        //Then
        assertThat(pictureFile).isEqualToComparingFieldByField(expected);
        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, filename)).isPresent();
        assertThatFileContainsData(pictureFile, DATA);
    }

    @Test
    public void moveWhenTargetFileAlreadyExistsInDatabase() throws Exception {
        //Given
        String targetFilename = "targetFilename.jpg";
        final byte[] SOME_DATA = targetFilename.getBytes();
        PictureFile anotherFile = pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, targetFilename).orElseThrow(IllegalStateException::new);
        anotherFile.setData(SOME_DATA);
        PictureFile expectedSource = PictureFileBuilder.getInstance().from(pictureFile).build();
        PictureFile expectedTarget = PictureFileBuilder.getInstance().from(anotherFile).build();

        //When
        assertThrows(PictureFileException.class,
                () -> instance.move(pictureFile, directory, targetFilename)
        );

        //Then
        assertThatFileContainsData(expectedSource, expectedSource.getData());
        assertThatFileContainsData(expectedTarget, expectedTarget.getData());
        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, expectedSource.getFilename())).isPresent();
        assertThat(pictureFileJpaRepository.findFirstByDirectoryAndFilename(directory, expectedTarget.getFilename())).isPresent();
    }

    @Test
    public void moveWhenTargetFileAlreadyExistsOnDisk() throws Exception {
        //Given
        String targetFilename = "targetFilename.jpg";
        Path pictureFileOnDisk = Paths.get(basedirPath + directory.getFullPath() + "/" + pictureFile.getFilename());
        final byte[] DATA = pictureFile.getFilename().getBytes();
        pictureFileJpaRepository.deleteById(201L);

        //When
        instance.move(pictureFile, directory, targetFilename);

        //Then
        assertThatFileContainsData(pictureFile, DATA);
        assertThat(pictureFile.getFilename()).isEqualTo(targetFilename);
        assertThat(pictureFile.getDirectory()).isEqualTo(directory);
        assertThat(Files.exists(pictureFileOnDisk)).isFalse();
    }

    @Test
    public void moveWhenWrongTargetFileName() {
        //Given
        String targetFilename = "/wrong/file.name";

        //When
        assertThrows(PictureFileException.class,
                () -> instance.move(pictureFile, directory, targetFilename)
        );
    }

    @Test
    public void moveWhenSourceFileNotExistsOnDisk() {
        //Given
        FileUtils.deleteQuietly(new File(basedirPath + pictureFile.getFullPath()));

        //When
        assertThrows(PictureFileException.class,
                () ->instance.move(pictureFile, directory, "newFileName.jpg")
        );
    }

    @Test
    public void moveBetweenDifferentUsers() {
        //Given
        Directory adminDirectory = directoryJpaRepository.findById(1000L).orElseThrow(IllegalStateException::new);

        //When
        assertThrows(PictureFileException.class,
                () ->instance.move(pictureFile, adminDirectory, "newName")
        );
    }

    @Test
    public void moveToAnotherDirectorySuccess() throws IOException {
        //Given
        String targetFilename = "nameAfterMove.jpg";
        final byte[] DATA = pictureFile.getFilename().getBytes();
        Directory targetDirectory = directoryJpaRepository.findById(2211L).orElseThrow(IllegalStateException::new);
        PictureFile expected = PictureFileBuilder.getInstance()
                .filename(targetFilename)
                .directory(targetDirectory)
                .data(DATA)
                .build();

        //When
        instance.move(pictureFile, targetDirectory, targetFilename);

        //Then
        assertThatFileContainsData(pictureFile, DATA);
        assertThat(pictureFile).isEqualToIgnoringGivenFields(expected, "id");
    }

    @Test
    public void findByIdSuccess() throws IOException {
        //Given
        PictureFile expected = PictureFileBuilder.getInstance().from(pictureFile).build();

        //When
        Optional<PictureFile> result = instance.findById(pictureFile.getId());

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent().get()
                .isEqualToComparingFieldByField(expected);
        if (result.isPresent()) {
            assertThatFileContainsData(result.get(), expected.getData());
        }
    }

    @Test
    public void findByIdFailure() {
        //When
        Optional<PictureFile> result = instance.findById(3232L);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void findByDirectorySuccess() {
        //Given
        PictureFile otherFile = PictureFileBuilder.getInstance()
                .id(201L).directory(directory).filename("targetFilename.jpg").data("targetFilename.jpg".getBytes())
                .build();

        //When
        List<PictureFile> result = instance.findByDirectory(directory);

        //Then
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(pictureFile, otherFile);
    }

    @Test
    public void findByDirectoryFailure() {
        //Given
        Directory directoryWithoutFiles = new DirectoryBuilder().id(2210L).owner(user).path("/folder/second").build();
        //When
        List<PictureFile> result = instance.findByDirectory(directoryWithoutFiles);
        //Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void findByDirectoryAndFilenameSuccess() {
        //When
        List<PictureFile> result = instance.findByDirectoryAndFilename(pictureFile.getDirectory(), pictureFile.getFilename());

        //Then
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactlyInAnyOrder(pictureFile);
    }

    @Test
    public void findByDirectoryAndFileNameFailure() {
        //When
        List<PictureFile> result = instance.findByDirectoryAndFilename(pictureFile.getDirectory(), "phantomFile.jpg");

        //Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }
}
