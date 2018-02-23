package ua.kiev.prog.photopond.user.registration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ua.kiev.prog.photopond.user.UserInfoService;

@TestConfiguration
@Profile("unitTest")
public class RegistrationControllerTestConfiguration {

    @Bean
    public RegistrationFormValidator registrationFormValidator(UserInfoService userInfoService){
        return new RegistrationFormValidatorImpl(userInfoService);
    }

}
