package ua.kiev.prog.photopond.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ua.kiev.prog.photopond.core.ApplicationConfiguration;

@Configuration
public class SecurityWebMVCConfiguration extends ApplicationConfiguration {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(new AccessUserDirectoryInterceptor()).addPathPatterns("/user/*/**");
    }
}
