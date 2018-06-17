package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;
import ua.kiev.prog.photopond.core.ApplicationConfiguration;

@Configuration
@DevOrProd
public class SecurityWebMVCConfiguration extends ApplicationConfiguration {
    private static final Logger LOG = LogManager.getLogger(SecurityWebMVCConfiguration.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LOG.debug("Add custom interceptors: {}, {}", AccessUserDirectoryInterceptor.class, UserInformationInterceptor.class);
        super.addInterceptors(registry);
        registry.addInterceptor(new AccessUserDirectoryInterceptor()).addPathPatterns("/user/*/**");
        registry.addInterceptor(new UserInformationInterceptor());
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        LOG.debug("Create bean with type {}", BCryptPasswordEncoder.class);
        return new BCryptPasswordEncoder();
    }
}
