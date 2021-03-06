package ua.kiev.prog.photopond.drive;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.Utils.TestUtils;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryJpaRepository;
import ua.kiev.prog.photopond.drive.directories.DirectoryRepository;
import ua.kiev.prog.photopond.drive.exception.DriveException;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;
import ua.kiev.prog.photopond.user.UserInfoServiceJpaImplITConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;
import static ua.kiev.prog.photopond.drive.DriveItemDTOMapper.toDTO;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "unitTest", "testMySQLDB", "test"})
@DataJpaTest
@EnableJpaAuditing
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@ContextConfiguration(classes = {
        UserInfoServiceJpaImplITConfiguration.class,
        DriveServiceImplITConfiguration.class
})
@DatabaseSetup("classpath:datasets/picturefile_dataset_IT.xml")
@Transactional
public class DriveServiceImplIT {

    private static final Logger LOG = LogManager.getLogger(DriveServiceImplIT.class);

    private final String ROOT_PATH = SEPARATOR;

    @Autowired
    private String generatedFoldersBaseDir;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserInfoJpaRepository userInfoRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private PictureFileRepository fileRepository;

    @Autowired
    private DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    private PictureFileJpaRepository pictureFileJpaRepository;

    private DriveServiceImpl instance;

    private UserInfo user;

    private Path basedirPath;

    @BeforeEach
    public void setUp() throws IOException {
        basedirPath = Paths.get(generatedFoldersBaseDir);

        instance = new DriveServiceImpl(directoryRepository, fileRepository, userInfoRepository);

        user = new UserInfoBuilder()
                .id(2L)
                .login("User")
                .password("password")
                .build();


        TestUtils.createDirectories(basedirPath, directoryJpaRepository);
        TestUtils.createPictureFiles(basedirPath, pictureFileJpaRepository);

    }

    @AfterEach
    void tearDown() {
        try {
            FileUtils.deleteDirectory(basedirPath.toFile());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
    }

    @Test
    public void userHasNoDirectory() throws IOException {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .id(2000L)
                .owner(user)
                .path(SEPARATOR)
                .build();
        Path expectedRootPath = Paths.get(basedirPath + expectedRoot.getFullPath());
        FileUtils.deleteDirectory(expectedRootPath.toFile());
        directoryJpaRepository.deleteAll(
                directoryJpaRepository.findByOwnerAndPathStartingWith(user, SEPARATOR)
        );

        List<DriveItemDTO> expected = emptyList();

        //When
        List<DriveItemDTO> result = instance.retrieveContent(user.getLogin(), SEPARATOR, true);

        //Then
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    public void rootDirectoryWithoutContent() throws IOException {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .id(2000L)
                .owner(user)
                .path(SEPARATOR)
                .build();
        Path expectedRootPath = Paths.get(basedirPath + expectedRoot.getFullPath());
        FileUtils.deleteDirectory(new File(expectedRootPath + "/first"));
        directoryJpaRepository.deleteAll(
                directoryJpaRepository.findByOwnerAndPathStartingWith(user, "/first")
        );
        FileUtils.deleteDirectory(new File(expectedRootPath + "/folder"));
        directoryJpaRepository.deleteAll(
                directoryJpaRepository.findByOwnerAndPathStartingWith(user, "/folder")
        );
        pictureFileJpaRepository.deleteAll(pictureFileJpaRepository.findByDirectory(expectedRoot));

        List<DriveItemDTO> expected = emptyList();

        //When
        List<DriveItemDTO> result = instance.retrieveContent(user.getLogin(), SEPARATOR, true);

        //Then
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    public void rootDirectoryWithContent() {
        //Given
        Directory expectedRoot = new DirectoryBuilder()
                .id(2000L)
                .owner(user)
                .path(SEPARATOR)
                .build();

        List<DriveItemDTO> expected = asList(
                toDTO(new DirectoryBuilder().id(2100L).owner(user).path("/first").build()),
                toDTO(new DirectoryBuilder().id(2200L).owner(user).path("/folder").build()),
                toDTO(PictureFileBuilder.getInstance().id(200L).filename("root.jpg").directory(expectedRoot).data("root.jpg".getBytes()).build()),
                toDTO(PictureFileBuilder.getInstance().id(201L).filename("targetFilename.jpg").directory(expectedRoot).data("targetFilename.jpg".getBytes()).build())
        );

        //When
        List<DriveItemDTO> result = instance.retrieveContent(user.getLogin(), SEPARATOR, true);

        //Then
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    public void notRootDirectoryWithContent() {
        //Given
        Directory root = new DirectoryBuilder()
                .id(2000L)
                .owner(user)
                .path(SEPARATOR)
                .build();

        List<DriveItemDTO> expected = asList(
                toDTO(new DirectoryBuilder().id(2110L).owner(user).path("/first/second").build()),
                toDTO(new DirectoryBuilder().id(2120L).owner(user).path("/first/folder(2)").build())
        );

        //When
        List<DriveItemDTO> result = instance.retrieveContent(user.getLogin(), "/first", true);

        //Then
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    void RetrieveDirectoriesSuccess() {
        //Given
        String baseUrl = "/api/" + user.getLogin() + "/directories";
        DirectoriesDTO expected = new DirectoriesDTO();
        expected.setCurrent(toDTO(new DirectoryBuilder().id(2100L).owner(user).path("/first").build(), baseUrl));
        expected.setParent(toDTO(new DirectoryBuilder().id(2000L).owner(user).path(SEPARATOR).build(), baseUrl));
        expected.setChildDirectories(asList(
                toDTO(new DirectoryBuilder().id(2110L).owner(user).path("/first/second").build(), baseUrl),
                toDTO(new DirectoryBuilder().id(2120L).owner(user).path("/first/folder(2)").build(), baseUrl)
        ));

        //When
        DirectoriesDTO directoriesDTO = instance.retrieveDirectories(user.getLogin(), "/first");

        //Then
        assertThat(directoriesDTO).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void addDirectorySuccess() {
        //Given
        Date start = new Date();
        DriveItemDTO expected = toDTO(new DirectoryBuilder()
                .path(buildPath(ROOT_PATH, "newDirectory"))
                .owner(user)
                .build()
        );

        //When
        DriveItemDTO result = instance.addDirectory(user.getLogin(), ROOT_PATH, expected.getName());

        //Then
        assertThat(result)
                .isEqualToIgnoringGivenFields(expected, "creationDate", "creationDateString");
        assertThat(result.getCreationDateString()).isNotNull();
        Calendar finish = Calendar.getInstance();
        finish.add(Calendar.SECOND, 1);
        assertThat(result.getCreationDate()).isBetween(start, finish.getTime());
    }

    @Test
    public void addDirectoryWhenLoginFailure() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.addDirectory("unknownLogin", ROOT_PATH, "/somePath")
        );
    }

    @Test
    public void addDirectoryWhenDirectoryAlreadyExists() {
        //Given
        Directory expected = new DirectoryBuilder().path(buildPath(ROOT_PATH, "first")).owner(user).build();

        //When

        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.addDirectory(user.getLogin(), ROOT_PATH, expected.getName())
        );
    }

    @Test
    public void moveDirectorySuccess() {
        //Given
        String sourcePath = "/first/folder(2)";
        Directory expected = new DirectoryBuilder()
                .id(2120L).owner(user).path("/folder/folder(2)").build();

        //When
        instance.moveDirectory(user.getLogin(), sourcePath, expected.getPath());

        //Then
        Optional<Directory> result = directoryRepository.findById(2120L);
        assertThat(result)
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(expected);
    }

    @Test
    public void moveDirectoryWhenUserNotExists() {
        //When

        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.moveDirectory("unknownUser", "/first/folder(2)", "/folder/folder(2)")
        );
    }

    @Test
    public void moveDirectoryWhenSourceDirectoryNotExists() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), "/phantomDirectory/folder(2)", "/folder/folder(2)")
        );
    }

    @Test
    public void moveDirectoryWhenTargetDirectoryNotExists() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), "/first/folder(2)", "/phantomDirectory/folder(2)")
        );
    }

    @Test
    public void renameDirectorySuccess() {
        //Given
        String sourcePath = "/first/folder(2)";
        Directory expected = new DirectoryBuilder()
                .id(2120L).owner(user).path("/first/newName").build();

        //When
        instance.moveDirectory(user.getLogin(), sourcePath, expected.getPath());

        //Then
        Optional<Directory> result = directoryRepository.findById(2120L);
        assertThat(result)
                .isPresent()
                .get()
                .isEqualToIgnoringGivenFields(expected);
    }

    @Test
    public void renameDirectoryWhenSourceDirectoryNotExists() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), "/phantomDirectory/folder(2)", "/phantomDirectory/newName")
        );
    }

    @Test
    public void renameDirectoryWhenTargetDirectoryContainsDirectoryWithSameName() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.moveDirectory(user.getLogin(), "/first/folder(2)", "/first/second")
        );
    }

    @Test
    public void deleteDirectorySuccess() {
        //Given
        String path = "/first";
        PictureFile file = fileRepository.save(PictureFileBuilder.getInstance()
                .directory(directoryRepository.findByOwnerAndPath(user, path).stream().findFirst().orElseThrow(IllegalStateException::new))
                .filename("somePictureFile.jpg")
                .data("somePictureFile.jpg".getBytes())
                .build()
        );

        //When
        instance.deleteDirectory(user.getLogin(), path);

        //Then
        entityManager.flush();
        entityManager.clear();
        assertThat(directoryRepository.findByOwnerAndPath(user, path))
                .isEmpty();
        assertThat(pictureFileJpaRepository.findById(file.getId()))
                .isNotPresent();
        assertThat(pictureFileJpaRepository.findById(file.getId()))
                .isNotPresent();
        assertThat(directoryJpaRepository.findByOwnerAndPathStartingWith(user, path))
                .isEmpty();
    }

    @Test
    public void deleteDirectoryWhenOwnerNotExist() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.deleteDirectory("unknownUser", "/someDirectory")
        );
    }

    @Test
    public void deleteDirectoryWhenSourceDirectoryNotExists() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.deleteDirectory(user.getLogin(), "/someDirectory")
        );
    }

    @Test
    public void retrievePictureFileDataSuccess() {
        //Given
        String filename = "root.jpg";
        final byte[] DATA = filename.getBytes();
        String filePath = buildPath(ROOT_PATH, filename);

        //When
        byte[] result = instance.retrievePictureFileData(user.getLogin(), filePath);

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualTo(DATA);
    }

    @Test
    public void retrievePictureFileDataWhenFileNotFound() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.retrievePictureFileData(user.getLogin(), buildPath(ROOT_PATH, "phantomFile.jpg"))
        );
    }

    @Test
    public void addPictureFileSuccess() {
        //Given
        Date start = new Date();
        String originalFilename = "awesomeFile.jpg";
        MultipartFile file = new MockMultipartFile("filename", originalFilename, "image/jpeg", originalFilename.getBytes());

        Directory directory = new DirectoryBuilder().id(2110L).owner(user).path("/first/second").build();
        PictureFile expectedFile = PictureFileBuilder.getInstance()
                .directory(directory)
                .filename(originalFilename)
                .data(originalFilename.getBytes())
                .build();
        DriveItemDTO expected = toDTO(expectedFile);

        //When
        DriveItemDTO result = instance.addPictureFile(user.getLogin(), directory.getPath(), file);

        //Then
        assertThat(result)
                .isNotNull()
                .isEqualToIgnoringGivenFields(expected, "creationDate", "creationDateString");
        assertThat(result.getCreationDateString()).isNotNull();
        Calendar finish = Calendar.getInstance();
        finish.add(Calendar.SECOND, 1);
        assertThat(result.getCreationDate()).isBetween(start, finish.getTime());
    }

    @Test
    public void moveAndRenamePictureFileSuccess() {
        //Given
        String filename = "root.jpg";
        Directory targetDirectory = new DirectoryBuilder().id(2110L).owner(user).path("/first/second").build();
        String targetFilename = "newName.jpg";
        PictureFile expected = PictureFileBuilder.getInstance()
                .id(200L)
                .directory(targetDirectory)
                .filename(targetFilename)
                .data(filename.getBytes())
                .build();

        //When
        instance.movePictureFile(user.getLogin(), buildPath(ROOT_PATH, filename), expected.getPath());

        //Then
        Optional<PictureFile> result = fileRepository.findById(200L);
        assertThat(result)
                .isPresent()
                .hasValue(expected);
    }

    @Test
    public void movePictureFileWhenFileNotFound() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.movePictureFile(user.getLogin(), "/unknownFile.jpg", "/first/newName.jpg")
        );
    }

    @Test
    public void deletePictureFileSuccess() {
        //When
        instance.deletePictureFile(user.getLogin(), buildPath(ROOT_PATH, "root.jpg"));

        //Then
        Optional<PictureFile> result = fileRepository.findById(200L);

        assertThat(result).isNotPresent();
    }

    @Test
    public void deletePictureFileWhenDeletedFileNotFound() {
        //When
        DriveException driveException = assertThrows(DriveException.class,
                () -> instance.deletePictureFile(user.getLogin(), buildPath(ROOT_PATH, "unknownPicture.jpg"))
        );
    }

    @Test
    void makeStatistics() {
        //Given
        DriveStatisticsDTO expected = new DriveStatisticsDTO(user.getLogin());
        expected.setDirectoriesSize(
                (long) "root.jpg".getBytes().length + "targetFilename.jpg".getBytes().length
        );
        expected.setPictureCount(2L);

        //When
        DriveStatisticsDTO statistics = instance.makeStatistics(user.getLogin());

        //Then
        assertThat(statistics).isEqualToComparingFieldByField(expected);
    }


    @Test
    void fullStatistics() {
        //Given
        DriveStatisticsDTO adminStatistics = new DriveStatisticsDTO("Administrator");
        adminStatistics.setDirectoriesSize((long) "qwerty.jpg".getBytes().length);
        adminStatistics.setPictureCount(1L);


        DriveStatisticsDTO userStatistics = new DriveStatisticsDTO(user.getLogin());
        userStatistics.setDirectoriesSize(
                (long) "root.jpg".getBytes().length + "targetFilename.jpg".getBytes().length);
        userStatistics.setPictureCount(2L);

        //When
        List<DriveStatisticsDTO> result = instance.fullStatistics();

        //Then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(adminStatistics, userStatistics);
    }
}