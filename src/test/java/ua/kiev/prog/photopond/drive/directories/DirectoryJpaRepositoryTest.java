package ua.kiev.prog.photopond.drive.directories;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.util.List;

import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "testDB", "test"})
@DataJpaTest
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:datasets/directories_dataset.xml")
public class DirectoryJpaRepositoryTest {

    private static final Logger LOG = LogManager.getLogger(DirectoryJpaRepositoryTest.class);

    @Autowired
    private DirectoryJpaRepository repository;

    @Autowired
    private UserInfoJpaRepository userInfoJpaRepository;

    @Test
    public void rename() {
        printDirectories(repository.findAll());
        UserInfo owner = userInfoJpaRepository.findById(1L).orElseThrow(IllegalStateException::new);
        List<Directory> directories = repository.findByOwnerAndPathStartingWith(owner, "/first");

        printDirectories(directories);
    }

    private void printDirectories(List<Directory> list) {
        for (Directory directory : list) {
            LOG.info(directory.toString());
        }
    }

}