package ua.kiev.prog.photopond.annotation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.AuthenticationManagerConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import ua.kiev.prog.photopond.security.SecurityWebMVCConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        SecurityWebMVCConfiguration.class,
        AuthenticationManagerConfiguration.class
})
public @interface DisableSecuritytAutoConfiguration {
}
