package ua.kiev.prog.photopond.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import ua.kiev.prog.photopond.user.UserInfoService;

@TestConfiguration
@Profile("unitTest")
public class UserInfoServiceMockConfiguration {

    @MockBean
    public UserInfoService userInfoService;

}
