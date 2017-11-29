package ua.kiev.prog.photopond;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import ua.kiev.prog.photopond.annotation.ITTest;

@RunWith(SpringRunner.class)
@ITTest
public class ApplicationRunnerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void contextLoad() {
        Assertions.assertThat(webApplicationContext)
                .isNotNull();
    }
}
