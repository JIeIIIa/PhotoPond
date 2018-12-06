package ua.kiev.prog.photopond.annotation.profile;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Profile(DISK_DATABASE_STORAGE)
@Inherited
public @interface DiskDatabaseStorage {
}
