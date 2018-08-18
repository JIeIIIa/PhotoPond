package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import ua.kiev.prog.photopond.annotation.profile.Dev;
import ua.kiev.prog.photopond.annotation.profile.Prod;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;
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
            UserInfoDTO adminDTO  = UserInfoDTOBuilder.getInstance()
                    .login(adminLogin)
                    .password(password)
                    .role(UserRole.ADMIN)
                    .build();

            createUserOrResetPassword(userInfoService, adminDTO);

            LOG.info("    =======================================");
            LOG.info("              Default administrators ");
            LOG.info("    =======================================");
            LOG.info("         login   : " + adminDTO.getLogin());
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
            UserInfoDTO adminDTO = UserInfoDTOBuilder.getInstance()
                    .login("admin")
                    .password("password")
                    .role(UserRole.ADMIN)
                    .build();
            createUserOrResetPassword(userInfoService, adminDTO);

            UserInfoDTO userDTO = UserInfoDTOBuilder.getInstance()
                    .login("user")
                    .password("useruser")
                    .build();
            createUserOrResetPassword(userInfoService, userDTO);

            UserInfoDTO deactivatedUserDTO = UserInfoDTOBuilder.getInstance()
                    .login("nonActiveUser")
                    .password("useruser")
                    .role(UserRole.DEACTIVATED)
                    .build();
            createUserOrResetPassword(userInfoService, deactivatedUserDTO);

            LOG.info("    =======================================");
            LOG.info("                Available users ");
            LOG.info("    =======================================");
            userInfoToLog(adminDTO);
            LOG.info("    ---------------------------------------");
            userInfoToLog(userDTO);
            LOG.info("    ---------------------------------------");
            userInfoToLog(deactivatedUserDTO);
            LOG.info("    =======================================");
        }
    }

    private static void createUserOrResetPassword(UserInfoService userInfoService, UserInfoDTO userDTO) {
        if (userInfoService.existsByLogin(userDTO.getLogin())) {
            userInfoService.resetPassword(userDTO.getLogin(), userDTO.getPassword());
        } else {
            userInfoService.addUser(userDTO);
        }
        userInfoService.findUserByLogin(userDTO.getLogin())
                .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve " + userDTO.getLogin() + " user"));
    }

    private static void userInfoToLog(UserInfoDTO userInfoDTO) {
        LOG.info("         login   : " + userInfoDTO.getLogin());
        LOG.info("         password: " + userInfoDTO.getPassword());
        LOG.info("         role    : " + userInfoDTO.getRole().name());
    }
}
