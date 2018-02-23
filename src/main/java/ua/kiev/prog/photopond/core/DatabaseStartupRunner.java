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
        public void run(String... strings) throws Exception {
            log.debug("Production database init");
            createUsers();
        }

        private void createUsers() {
            UserInfo admin = userInfoService.getUserByLogin("SuperPhotoPondAdmin");
            if (admin == null) {
                admin = new UserInfoBuilder()
                        .login("admin")
                        .password(generatePassword())
                        .role(UserRole.ADMIN)
                        .build();
                userInfoService.addUser(admin);
            } else if (!admin.getRole().equals(UserRole.ADMIN)) {
                log.info("!!! Default ADMIN was disabled !!!");
                return;
            }
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
        public void run(String... strings) throws Exception {
            log.debug("Dev database init");
            createUsers();
        }

        private void createUsers() {
            UserInfo admin = new UserInfoBuilder()
                    .login("admin")
                    .password("password")
                    .role(UserRole.ADMIN)
                    .build();
            userInfoService.addUser(admin);
            UserInfo user = new UserInfoBuilder()
                    .login("user")
                    .password("useruser")
                    .build();
            userInfoService.addUser(user);
            UserInfo deactivatedUser = new UserInfoBuilder()
                    .login("nonActiveUser")
                    .password("useruser")
                    .role(UserRole.DEACTIVATED)
                    .build();
            userInfoService.addUser(deactivatedUser);
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
