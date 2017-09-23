package ua.kiev.prog.photopond;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationRunnerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void contextLoad() {
        Assert.assertNotNull("failure - webApplicationContext null", webApplicationContext);
    }
}
