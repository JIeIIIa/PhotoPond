package ua.kiev.prog.photopond.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
@EnableWebMvc
@ComponentScan("ua.kiev.prog.photopond")
public class ApplicationConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {
    private static Logger log = LogManager.getLogger(ApplicationConfig.class);

    @Autowired
    ApplicationContext applicationContext;

    public ApplicationConfig() {
        super();
        log.debug("Create instance of " + this.getClass().getName());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.trace("Set application context in " + this.getClass().getName());
        this.applicationContext = applicationContext;
    }

    @Bean
    public ThymeleafViewResolver viewResolver() {
        log.trace("Start configuration view resolver");
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        log.trace("Finish configuration view resolver");
        return viewResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        log.trace("Start configuration template engine");
        // SpringTemplateEngine automatically applies SpringStandardDialect and
        // enables Spring's own MessageSource message resolution mechanisms.
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
        // speed up execution in most scenarios, but might be incompatible
        // with specific cases when expressions in one template are reused
        // across different data types, so this flag is "false" by default
        // for safer backwards compatibility.
        templateEngine.setEnableSpringELCompiler(true);
        log.trace("Finish configuration template engine");
        return templateEngine;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        log.trace("Start configuration template resolver");
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // Template cache is true by default. Set to false if you want
        // templates to be automatically updated when modified.
        templateResolver.setCacheable(false);
        log.trace("Finish configuration template resolver");
        return templateResolver;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        log.trace("Setup message source");
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        return messageSource;
    }
}
