package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan("ua.kiev.prog.photopond")
public class ApplicationConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {
    private static Logger log = LogManager.getLogger(ApplicationConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    public ApplicationConfiguration() {
        super();
        log.debug("Create instance of " + this.getClass().getName());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.trace("Set application context in " + this.getClass().getName());
        this.applicationContext = applicationContext;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/libs/**").addResourceLocations("/libs/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
    }
}
