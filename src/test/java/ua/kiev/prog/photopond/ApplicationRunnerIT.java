package ua.kiev.prog.photopond;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import ua.kiev.prog.photopond.annotation.IntegrationTest;

import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@RunWith(SpringRunner.class)
@IntegrationTest
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE})
public class ApplicationRunnerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void contextLoad() {
        Assertions.assertThat(webApplicationContext)
                .isNotNull();
    }
}
