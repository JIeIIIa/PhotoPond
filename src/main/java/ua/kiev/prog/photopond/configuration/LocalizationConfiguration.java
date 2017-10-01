package ua.kiev.prog.photopond.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

@Configuration
public class LocalizationConfiguration{
    private static Logger log = LogManager.getLogger(LocalizationConfiguration.class);

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        log.trace("Setup message source");
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("/WEB-INF/i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
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
