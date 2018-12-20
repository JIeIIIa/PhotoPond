package ua.kiev.prog.photopond.core;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

import java.util.Arrays;

import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.*;

public class ProfileCheckApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        ConfigurableEnvironment environment = configurableApplicationContext.getEnvironment();

        String[] profiles = {PROD, DEV};
        if (countActiveProfiles(environment, profiles) != 1) {
            throw new ApplicationContextException("Choose exact ONE profile of " + Arrays.toString(profiles));
        }

        String[] dataStorageProfile = {DISK_DATABASE_STORAGE, DATABASE_STORAGE};
        if (countActiveProfiles(environment, dataStorageProfile) != 1) {
            throw new ApplicationContextException("Choose only ONE profile to store data of " + Arrays.toString(dataStorageProfile));
        }

    }

    private long countActiveProfiles(ConfigurableEnvironment environment, String... profiles) {
        return Arrays.stream(profiles)
                .filter(p -> environment.acceptsProfiles(Profiles.of(p)))
                .count();
    }
}
