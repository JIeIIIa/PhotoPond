package ua.kiev.prog.photopond.annotation;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@Profile("localTest")
public @interface LocalTestProfile {
}
