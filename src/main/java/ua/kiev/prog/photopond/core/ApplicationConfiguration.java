package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import static org.springframework.core.env.Profiles.of;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.PROD;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
public class ApplicationConfiguration implements WebMvcConfigurer {
    private static Logger log = LogManager.getLogger(ApplicationConfiguration.class);

    private Environment environment;

    public ApplicationConfiguration() {
        log.debug("Create instance of " + this.getClass().getName());
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/libs/**")
                .addResourceLocations("classpath:/static/libs/");
        registry.addResourceHandler("/pic/**")
                .addResourceLocations("classpath:/static/pic/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
        if (environment.acceptsProfiles(of(PROD))) {
            useMinifiedFiles(registry);
        } else {
            useOriginalFile(registry);
        }
    }

    private void useMinifiedFiles(ResourceHandlerRegistry registry) {
        log.info("Use minified css and js files");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/min/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/min/js/");
    }

    private void useOriginalFile(ResourceHandlerRegistry registry) {
        log.info("Use NOT minified css and js files");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
