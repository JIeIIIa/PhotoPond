package ua.kiev.prog.photopond.user;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("unitTest")
public class UserInfoServiceJpaServiceImplITConfiguration {

    @Bean
    public UserInfoServiceJpaImpl userInfoServiceJpa(UserInfoJpaRepository userInfoJpaRepository) {
        return new UserInfoServiceJpaImpl(userInfoJpaRepository);
    }
}
