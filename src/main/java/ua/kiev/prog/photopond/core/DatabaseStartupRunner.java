package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import ua.kiev.prog.photopond.annotation.profile.Dev;
import ua.kiev.prog.photopond.annotation.profile.Prod;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

@Configuration
public class DatabaseStartupRunner {
    private static Logger log = LogManager.getLogger(DatabaseStartupRunner.class);

    @Configuration
    @Prod
    public static class ProductionDatabaseStartupRunner implements CommandLineRunner {
        private final UserInfoService userInfoService;

        @Autowired
        public ProductionDatabaseStartupRunner(UserInfoService userInfoService) {
            this.userInfoService = userInfoService;
        }

        @Override
        public void run(String... strings) {
            log.debug("Production database init");
            createUsers();
        }

        private void createUsers() {
            UserInfo admin;
            if (!userInfoService.existByLogin("SuperPhotoPondAdmin")) {
                admin = new UserInfoBuilder()
                        .login("SuperPhotoPondAdmin")
                        .password(generatePassword())
                        .role(UserRole.ADMIN)
                        .build();
                userInfoService.addUser(admin);
            }
            admin = userInfoService.getUserByLogin("SuperPhotoPondAdmin").get();
            log.info("    =======================================");
            log.info("                Available users ");
            log.info("    =======================================");
            userInfoToLog(admin);
            log.info("    =======================================");
        }

        private String generatePassword() {
            return "password";
        }
    }

    @Configuration
    @Dev
    public static class DeveloperDatabaseStartupRunner implements CommandLineRunner {

        private final UserInfoService userInfoService;

        @Autowired
        public DeveloperDatabaseStartupRunner(UserInfoService userInfoService) {
            this.userInfoService = userInfoService;
        }

        @Override
        public void run(String... strings) {
            log.debug("Dev database init");
            createUsers();
        }

        private void createUsers() {
            UserInfo admin;
            if (!userInfoService.existByLogin("admin")) {
                admin = new UserInfoBuilder()
                        .login("admin")
                        .password("password")
                        .role(UserRole.ADMIN)
                        .build();
                userInfoService.addUser(admin);
            }
            admin = userInfoService.getUserByLogin("admin").get();

            UserInfo user;
            if (!userInfoService.existByLogin("user")) {
                user = new UserInfoBuilder()
                        .login("user")
                        .password("useruser")
                        .build();
                userInfoService.addUser(user);
            }
            user = userInfoService.getUserByLogin("user").get();

            UserInfo deactivatedUser;
            if (!userInfoService.existByLogin("nonActiveUser")) {
                deactivatedUser = new UserInfoBuilder()
                        .login("nonActiveUser")
                        .password("useruser")
                        .build();
                userInfoService.addUser(deactivatedUser);
            }
            deactivatedUser = userInfoService.getUserByLogin("nonActiveUser").get();

            log.info("    =======================================");
            log.info("                Available users ");
            log.info("    =======================================");
            userInfoToLog(admin);
            log.info("    ---------------------------------------");
            userInfoToLog(user);
            log.info("    ---------------------------------------");
            userInfoToLog(deactivatedUser);
            log.info("    =======================================");
        }
    }

    private static void userInfoToLog(UserInfo userInfo) {
        log.info("         login   : " + userInfo.getLogin());
        log.info("         password: " + userInfo.getPassword());
        log.info("         role    : " + userInfo.getRole().name());
    }
}
