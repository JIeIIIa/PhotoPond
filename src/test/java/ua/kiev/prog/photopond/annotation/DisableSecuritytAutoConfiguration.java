package ua.kiev.prog.photopond.annotation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import ua.kiev.prog.photopond.security.SecurityWebMVCConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        SecurityWebMVCConfiguration.class
})
public @interface DisableSecuritytAutoConfiguration {
}
