package ua.kiev.prog.photopond.annotation.profile;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Profile(DEV)
@Inherited
public @interface Dev {
}
