package ua.kiev.prog.photopond;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;
import ua.kiev.prog.photopond.annotation.IntegrationTest;

import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@ExtendWith(SpringExtension.class)
@IntegrationTest
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "test"})
public class ApplicationRunnerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void contextLoad() {
        Assertions.assertThat(webApplicationContext)
                .isNotNull();
    }
}
