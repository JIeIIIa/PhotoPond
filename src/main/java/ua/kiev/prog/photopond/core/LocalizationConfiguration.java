package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class LocalizationConfiguration{
    private static Logger log = LogManager.getLogger(LocalizationConfiguration.class);

    @Value("${spring.messages.cache-duration}")
    private Duration cacheDuration;

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        log.trace("Setup message source");
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/i18n/messages", "classpath:/i18n/validator");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds((int) cacheDuration.getSeconds());
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        log.trace("Start configuration template resolver");
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(new Locale("en"));
        resolver.setCookieName("PhotopondLocaleCookie");
        resolver.setCookieMaxAge(4800);
        log.trace("Finish configuration template resolver");
        return resolver;
    }

}
