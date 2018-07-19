package ua.kiev.prog.photopond.drive;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.Utils.TestUtils;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryBuilder;
import ua.kiev.prog.photopond.drive.directories.DirectoryDiskAndDatabaseRepository;
import ua.kiev.prog.photopond.drive.directories.DirectoryJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFileJpaRepository;
import ua.kiev.prog.photopond.drive.pictures.PictureFileRepository;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserInfoServiceJpaImplITConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.kiev.prog.photopond.drive.DriveItemDTOMapper.toDTO;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "unitTest", "testMySQLDB"})
@DataJpaTest
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@ContextConfiguration(classes = {
        UserInfoServiceJpaImplITConfiguration.class,
        DriveServiceImplITConfiguration.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DatabaseSetup("classpath:datasets/picturefile_dataset_IT.xml")
@Transactional
public class DriveServiceImplIT {
    private final String ROOT_PATH = SEPARATOR;

    @Value(value = "${folders.basedir.location}")
    private String foldersBasedir;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private DirectoryDiskAndDatabaseRepository directoryRepository;

    @Autowired
    private PictureFileRepository fileRepository;

    @Autowired
    private DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    private PictureFileJpaRepository pictureFileJpaRepository;

    private DriveServiceImpl instance;

    private UserInfo user;

    private Path basedirPath;

    @Before
    public void setUp() throws IOException {
        instance = new DriveServiceImpl(directoryRepository, fileRepository, userInfoService);

        user = new UserInfoBuilder()
                .id(2)
                .login("User")
                .password("password")
                .build();

        basedirPath = Paths.get(foldersBasedir);

        TestUtils.createDirectories(basedirPath, directoryJpaRepository);
        TestUtils.createPictureFiles(basedirPath, pictureFileJpaRepository);

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
    public void addDirectorySuccess() {
        //Given
        DriveItemDTO expected = toDTO(new DirectoryBuilder()
                .path(buildPath(ROOT_PATH, "newDirectory"))
                .owner(user)
                .build()
        );

        //When
        DriveItemDTO result = instance.addDirectory(user.getLogin(), ROOT_PATH, expected.getName());

        //Then
        assertThat(result).isEqualToComparingFieldByField(expected);
    }

    @Test(expected = DriveException.class)
    public void addDirectoryWhenLoginFailure() {
        //When
        instance.addDirectory("unknownLogin", ROOT_PATH, "/somePath");
    }

    @Test(expected = DriveException.class)
    public void addDirectoryWhenDirectoryAlreadyExists() {
        //Given
        Directory expected = new DirectoryBuilder().path(buildPath(ROOT_PATH, "first")).owner(user).build();

        //When
        instance.addDirectory(user.getLogin(), ROOT_PATH, expected.getName());
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

    @Test(expected = DriveException.class)
    public void moveDirectoryWhenUserNotExists() {
        //When
        instance.moveDirectory("unknownUser", "/first/folder(2)", "/folder/folder(2)");
    }

    @Test(expected = DriveException.class)
    public void moveDirectoryWhenSourceDirectoryNotExists() {
        //When
        instance.moveDirectory(user.getLogin(), "/phantomDirectory/folder(2)", "/folder/folder(2)");
    }

    @Test(expected = DriveException.class)
    public void moveDirectoryWhenTargetDirectoryNotExists() {
        //When
        instance.moveDirectory(user.getLogin(), "/first/folder(2)", "/phantomDirectory/folder(2)");
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

    @Test(expected = DriveException.class)
    public void renameDirectoryWhenSourceDirectoryNotExists() {
        //When
        instance.moveDirectory(user.getLogin(), "/phantomDirectory/folder(2)", "/phantomDirectory/newName");
    }

    @Test(expected = DriveException.class)
    public void renameDirectoryWhenTargetDirectoryContainsDirectoryWithSameName() {
        //When
        instance.moveDirectory(user.getLogin(), "/first/folder(2)", "/first/second");
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

    @Test(expected = DriveException.class)
    public void deleteDirectoryWhenOwnerNotExist() {
        //When
        instance.deleteDirectory("unknownUser", "/someDirectory");
    }

    @Test(expected = DriveException.class)
    public void deleteDirectoryWhenSourceDirectoryNotExists() {
        //When
        instance.deleteDirectory(user.getLogin(), "/someDirectory");
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

    @Test(expected = DriveException.class)
    public void retrievePictureFileDataWhenFileNotFound() {
        //When
        instance.retrievePictureFileData(user.getLogin(), buildPath(ROOT_PATH, "phantomFile.jpg"));
    }

    @Test
    public void addPictureFileSuccess() {
        //Given
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
                .isEqualToComparingFieldByField(expected);
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

    @Test(expected = DriveException.class)
    public void movePictureFileWhenFileNotFound() {
        //When
        instance.movePictureFile(user.getLogin(), "/unknownFile.jpg", "/first/newName.jpg");
    }

    @Test
    public void deletePictureFileSuccess() {
        //When
        instance.deletePictureFile(user.getLogin(), buildPath(ROOT_PATH, "root.jpg"));

        //Then
        Optional<PictureFile> result = fileRepository.findById(200L);

        assertThat(result).isNotPresent();
    }

    @Test(expected = DriveException.class)
    public void deletePictureFileWhenDeletedFileNotFound() {
        //When
        instance.deletePictureFile(user.getLogin(), buildPath(ROOT_PATH, "unknownPicture.jpg"));
    }
}