package ua.kiev.prog.photopond.drive.directories;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import ua.kiev.prog.photopond.drive.exception.DirectoryModificationException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.drive.directories.Directory.SEPARATOR;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;


@ExtendWith(SpringExtension.class)
@ActiveProfiles({DEV, DATABASE_STORAGE, "test"})
@DataJpaTest
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:datasets/directories_dataset_IT.xml")
@Transactional
public class DirectoryDatabaseRepositoryImplIT {

    private static final Logger LOG = LogManager.getLogger(DirectoryDatabaseRepositoryImplIT.class);

    @Autowired
    private DirectoryJpaRepository directoryJpaRepository;

    @Autowired
    private UserInfoJpaRepository userInfoJpaRepository;

    private DirectoryDatabaseRepositoryImpl instance;

    private UserInfo user;
    private Directory directory;

    @BeforeEach
    public void setUp() {
        instance = new DirectoryDatabaseRepositoryImpl(directoryJpaRepository);

        user = userInfoJpaRepository.findByLogin("User")
                .orElseThrow(() -> new IllegalStateException("Failure retrieve User"));
        directory = new DirectoryBuilder().id(2100L).owner(user).path("/first").build();
    }

    @Test
    public void saveNullDirectory() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.save(null)
        );
    }

    @Test
    public void saveWhenDirectoryNotExists() {
        //Given
        String newPath = "/first/second/third/newFolder";
        Directory dir = new DirectoryBuilder().owner(user).path(newPath).build();
        long count = directoryJpaRepository.countByOwner(user);

        //When
        instance.save(dir);

        //Then
        List<Directory> saved = directoryJpaRepository.findByOwnerAndPath(user, newPath);
        assertThat(saved)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        assertThat(directoryJpaRepository.countByOwner(user)).isEqualTo(count + 1);
    }

    @Test
    public void saveWhenParentDirectoryNotExists() {
        //Given
        Directory directory = new DirectoryBuilder().owner(user).path("/phantomDirectory/newFolder").build();

        //When
        assertThrows(DirectoryModificationException.class,
                () -> instance.save(directory)
        );
    }

    @Test
    public void deleteNullDirectory() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.delete(null)
        );
    }

    @Test
    public void deleteEmptyDirectory() {
        //Given
        directory = new DirectoryBuilder().id(2111L).owner(user).path("/first/second/third").build();
        long count = directoryJpaRepository.countByOwner(user);

        //When
        instance.delete(directory);

        //Then
        assertThat(directoryJpaRepository.findById(2111L))
                .isNotPresent();
        assertThat(directoryJpaRepository.countByOwner(user))
                .isEqualTo(count - 1);
    }

    @Test
    public void deleteWithSubdirectories() {
        //Given
        directory = new DirectoryBuilder().id(2110L).owner(user).path("/first/second").build();
        Directory subDirectory = new DirectoryBuilder().id(2111L).owner(user).path("/first/second/third").build();
        long count = directoryJpaRepository.countByOwner(user);

        //When
        instance.delete(directory);

        //Then
        assertThat(directoryJpaRepository.findById(2110L))
                .isNotPresent();
        assertThat(directoryJpaRepository.findById(2111L))
                .isNotPresent();
        assertThat(directoryJpaRepository.countByOwner(user))
                .isEqualTo(count - 2);
    }

    @Test
    public void renameTargetEqualsSource() throws DirectoryModificationException {
        //Given
        directory = new DirectoryBuilder().id(2110L).owner(user).path("/first/second").build();
        Directory expected = new DirectoryBuilder().from(directory).build();

        //When
        instance.rename(directory, directory.getPath());

        //Then
        assertThat(directory).isEqualTo(expected);
    }

    @Test
    public void renameNullDirectory() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.rename(null, "/someNewName")
        );
    }

    @Test
    public void renameSuccess() {
        //Given
        String targetPath = buildPath(directory.parentPath(), "target");

        List<Directory> directories = new LinkedList<>();
        directories.add(
                new DirectoryBuilder().from(
                        directoryJpaRepository.findById(2110L).orElseThrow(() -> new IllegalStateException("Directory not found"))
                ).build());
        directories.add(
                new DirectoryBuilder().from(
                        directoryJpaRepository.findById(2111L).orElseThrow(() -> new IllegalStateException("Directory not found"))
                ).build());
        long count = directoryJpaRepository.countByOwner(user);


        //When
        instance.rename(directory, targetPath);

        //Then
        assertThat(directoryJpaRepository.countByOwner(user)).isEqualTo(count);
        List<Directory> afterRename = directoryJpaRepository.findByOwnerAndPathStartingWith(user, targetPath);
        assertThat(afterRename).hasSize(5);
    }

    @Test
    public void renameWhenTargetDirectoryAlreadyExistsInDataBase() {
        //Given
        String targetPath = "/folder";

        //When
        assertThrows(DirectoryModificationException.class,
                () -> instance.rename(directory, targetPath)
        );
    }

    @Test
    public void moveNullDirectory() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.move(null, directory)
        );
    }

    @Test
    public void moveToNullDirectory() {
        //When
        assertThrows(IllegalArgumentException.class,
                () -> instance.move(directory, null)
        );
    }

    @Test
    public void moveTargetDirectoryEqualsCurrentDirectory() {
        //Given
        Directory expectedDirectory = new DirectoryBuilder().from(directory).build();
        long count = directoryJpaRepository.countByOwner(user);

        //When
        instance.move(directory, directory);

        //Then
        assertThat(directory).isEqualToComparingFieldByFieldRecursively(expectedDirectory);
        assertThat(directoryJpaRepository.countByOwner(user)).isEqualTo(count);
    }

    @Test
    public void moveBetweenDifferentUsers() {
        //Given
        Directory directoryWithAnotherOwner = new DirectoryBuilder()
                .id(1200L)
                .owner(userInfoJpaRepository.findById(1L).orElseThrow(IllegalStateException::new))
                .path("/folder")
                .build();
        //When
        assertThrows(DirectoryModificationException.class,
                () -> instance.move(directory, directoryWithAnotherOwner)
        );
    }

    @Test
    public void moveTargetDirectoryContainsSubDirectoryWithSameName() {
        //Given
        Directory target = new DirectoryBuilder().id(2200L).owner(user).path("/folder").build();

        //When
        assertThrows(DirectoryModificationException.class,
                () -> instance.move(directory, target)
        );
    }

    @Test
    public void moveToSubDirectory() throws DirectoryModificationException {
        //Given
        Directory subDirectory = new DirectoryBuilder().id(2111L).owner(user).path("/first/second/third").build();

        //When
        assertThrows(DirectoryModificationException.class,
                () -> instance.move(directory, subDirectory)
        );
    }

    @Test
    public void moveSuccess() {
        //Given
        List<Directory> subDirectories = directoryJpaRepository.findByOwnerAndPathStartingWith(user, directory.getPath())
                .stream()
                .map(d -> new DirectoryBuilder().from(d).build())
                .collect(toList());
        String targetPath = "/folder/folder(2)/folder";
        Directory target = new DirectoryBuilder().id(2211L).owner(user).path(targetPath).build();
        long count = directoryJpaRepository.countByOwner(user);

        //When
        instance.move(directory, target);

        //Then
        assertThat(directoryJpaRepository.countByOwner(user)).isEqualTo(count);
        assertThat(directoryJpaRepository.findByOwnerAndPathStartingWith(user, "/first")).isEmpty();
        for (Directory dir : subDirectories) {
            Optional<Directory> movedDirectory = directoryJpaRepository.findById(dir.getId());

            assertThat(movedDirectory)
                    .isPresent()
                    .get()
                    .matches(d -> Objects.equals(d.getName(), dir.getName()))
                    .matches(d -> Objects.equals(d.getPath(), buildPath(targetPath, dir.getPath())));
        }
    }

    @Test
    public void findTopLevelSubDirectoriesForRoot() {
        // Given
        Directory root = new DirectoryBuilder().id(2000L).owner(user).path(SEPARATOR).build();

        // When
        List<Directory> result = instance.findTopLevelSubDirectories(root);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("path")
                .contains("/first", "/folder");
    }

    @Test
    public void findTopLevelSubDirectoriesForNonRoot() {
        // When
        List<Directory> result = instance.findTopLevelSubDirectories(directory);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting("path")
                .contains("/first/second", "/first/folder(2)", "/first/dir");
    }

    @Test
    public void countByOwner() {
        //When
        long count = instance.countByOwner(user);

        //Then
        assertThat(count).isEqualTo(10);
    }

    @Test
    public void findByOwnerAndPathSuccess() {
        //Given
        String path = "/folder";
        Directory expected = new DirectoryBuilder().id(2200L).owner(user).path(path).build();

        //When
        List<Directory> result = instance.findByOwnerAndPath(user, path);

        //Then
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(expected);
    }

    @Test
    public void findByOwnerAndPathFailure() {
        //When
        List<Directory> result = instance.findByOwnerAndPath(user, "/phantom/path");

        //Then
        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void findByIdSuccess() {
        //When
        Optional<Directory> result = instance.findById(directory.getId());

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(directory);
    }

    @Test
    public void findByIdFailure() {
        //When
        Optional<Directory> result = instance.findById(98765L);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
    }


    @Test
    public void findByOwnerAndIdSuccess() {
        //When
        Optional<Directory> result = instance.findByOwnerAndId(directory.getOwner(), directory.getId());

        //Then
        assertThat(result)
                .isNotNull()
                .isPresent()
                .hasValue(directory);
    }

    @Test
    public void findByOwnerAndIdFailure() {
        //When
        Optional<Directory> result = instance.findByOwnerAndId(user, 987L);

        //Then
        assertThat(result)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    public void directoryExists() {
        //When
        boolean result = instance.exists(user, "/first");

        //Then
        assertThat(result).isTrue();
    }

    @Test
    public void directoryNotExists() {
        //When
        boolean result = instance.exists(user, "/phantomDirectory");

        //Then
        assertThat(result).isFalse();
    }
}
