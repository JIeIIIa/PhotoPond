package ua.kiev.prog.photopond.annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@ActiveProfiles({"dev", "localTest"})
@ContextConfiguration(classes = {
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ComponentScan("ua.kiev.prog.photopond")
public @interface SecurityTest {
}


