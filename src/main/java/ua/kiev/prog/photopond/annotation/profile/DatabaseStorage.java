package ua.kiev.prog.photopond.annotation.profile;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Profile(ProfileConstants.DATABASE_STORAGE)
@Inherited
public @interface DatabaseStorage {
}
