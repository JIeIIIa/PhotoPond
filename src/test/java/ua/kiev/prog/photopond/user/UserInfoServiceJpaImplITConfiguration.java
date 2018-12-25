package ua.kiev.prog.photopond.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@TestConfiguration
@Profile({"unitTest", "test"})
public class UserInfoServiceJpaImplITConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = BCryptPasswordEncoder.class)
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserInfoServiceJpaImpl userInfoServiceJpaImpl(UserInfoJpaRepository userInfoJpaRepository) {
        return new UserInfoServiceJpaImpl(userInfoJpaRepository, passwordEncoder());
    }
}
