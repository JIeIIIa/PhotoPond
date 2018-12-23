package ua.kiev.prog.photopond.drive.directories;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
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
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "testDB"})
@DataJpaTest
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:datasets/directories_dataset.xml")
public class DirectoryJpaRepositoryTest {
    @Autowired
    private DirectoryJpaRepository repository;

    @Autowired
    private UserInfoJpaRepository userInfoJpaRepository;

    @Test
    public void rename() {
        printAllDirectories(repository.findAll());
        UserInfo owner = userInfoJpaRepository.findById(1L).orElseThrow(IllegalStateException::new);
        List<Directory> directories = repository.findByOwnerAndPathStartingWith(owner, "/first");


        System.out.println("After renaming:");
        printAllDirectories(directories);
    }

    private void printAllDirectories(List<Directory> list) {
         repository.findAll();
        for (Directory directory : list) {
            System.out.println(directory.toString());
        }
    }

}