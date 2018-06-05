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
    private static final Logger LOG = LogManager.getLogger(DatabaseStartupRunner.class);

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
            LOG.info("Production database init");
            createUsers();
        }

        private void createUsers() {
            String password = generatePassword();
            String adminLogin = "PhotoPondSuperAdmin";
            UserInfo admin;

            if (!userInfoService.existsByLogin(adminLogin)) {
                LOG.debug("Create user with UserRole.ADMIN");
                admin = new UserInfoBuilder()
                        .login(adminLogin)
                        .password(password)
                        .role(UserRole.ADMIN)
                        .build();
                userInfoService.addUser(admin);
            } else {
                userInfoService.setNewPassword(adminLogin, password);
            }
            admin = userInfoService.findUserByLogin(adminLogin)
                    .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve 'SuperPhotoPondAdmin' user"));
            LOG.info("    =======================================");
            LOG.info("              Default administrators ");
            LOG.info("    =======================================");
            LOG.info("         login   : " + admin.getLogin());
            LOG.info("         password: " + password);
            LOG.info("    =======================================");
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
            LOG.info("Dev database init");
            createUsers();
        }

        private void createUsers() {
            UserInfo admin;
            if (!userInfoService.existsByLogin("admin")) {
                admin = new UserInfoBuilder()
                        .login("admin")
                        .password("password")
                        .role(UserRole.ADMIN)
                        .build();
                userInfoService.addUser(admin);
            }
            admin = userInfoService.findUserByLogin("admin")
                    .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve 'admin' user"));

            UserInfo user;
            if (!userInfoService.existsByLogin("user")) {
                user = new UserInfoBuilder()
                        .login("user")
                        .password("useruser")
                        .build();
                userInfoService.addUser(user);
            }
            user = userInfoService.findUserByLogin("user")
                    .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve 'user' user"));

            UserInfo deactivatedUser;
            if (!userInfoService.existsByLogin("nonActiveUser")) {
                deactivatedUser = new UserInfoBuilder()
                        .login("nonActiveUser")
                        .password("useruser")
                        .build();
                userInfoService.addUser(deactivatedUser);
            }
            deactivatedUser = userInfoService.findUserByLogin("nonActiveUser")
                    .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve 'nonActiveUser' user"));

            LOG.info("    =======================================");
            LOG.info("                Available users ");
            LOG.info("    =======================================");
            userInfoToLog(admin);
            LOG.info("    ---------------------------------------");
            userInfoToLog(user);
            LOG.info("    ---------------------------------------");
            userInfoToLog(deactivatedUser);
            LOG.info("    =======================================");
        }
    }

    private static void userInfoToLog(UserInfo userInfo) {
        LOG.info("         login   : " + userInfo.getLogin());
        LOG.info("         password: " + userInfo.getPassword());
        LOG.info("         role    : " + userInfo.getRole().name());
    }
}
