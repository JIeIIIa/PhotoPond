package ua.kiev.prog.photopond.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.core.BindingErrorResolverImpl;
import ua.kiev.prog.photopond.core.LocalizationConfiguration;

@TestConfiguration
@ContextConfiguration(classes = {
        LocalizationConfiguration.class
})
public class WebMvcTestContextConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = BindingErrorResolver.class)
    public BindingErrorResolver bindingErrorResolver(MessageSource messageSource) {
        return new BindingErrorResolverImpl(messageSource);
    }
}
