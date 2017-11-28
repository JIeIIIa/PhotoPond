package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;
import ua.kiev.prog.photopond.core.ApplicationConfiguration;

@Configuration
@DevOrProd
public class SecurityWebMVCConfiguration extends ApplicationConfiguration {
    private static Logger log = LogManager.getLogger(SecurityWebMVCConfiguration.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("Add custom interceptors: AccessUserDirectoryInterceptor, UserInformationInterceptor");
        super.addInterceptors(registry);
        registry.addInterceptor(new AccessUserDirectoryInterceptor()).addPathPatterns("/user/*/**");
        registry.addInterceptor(new UserInformationInterceptor());
    }
}
