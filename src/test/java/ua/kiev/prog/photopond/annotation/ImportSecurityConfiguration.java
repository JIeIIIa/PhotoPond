package ua.kiev.prog.photopond.annotation;

import org.springframework.context.annotation.Import;
import ua.kiev.prog.photopond.security.SecurityConfiguration;
import ua.kiev.prog.photopond.security.SecurityWebMVCConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@Import(value = {
        SecurityConfiguration.class,
        SecurityWebMVCConfiguration.class
})
public @interface ImportSecurityConfiguration {
}


