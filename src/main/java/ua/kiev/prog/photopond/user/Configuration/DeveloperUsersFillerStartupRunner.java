package ua.kiev.prog.photopond.user.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import ua.kiev.prog.photopond.annotation.profile.Dev;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

@Configuration
@Order(value = Integer.MAX_VALUE - 1000)
@Dev
public class DeveloperUsersFillerStartupRunner extends AbstractUsersFillerStartupRunner {
    private static final Logger LOG = LogManager.getLogger(DeveloperUsersFillerStartupRunner.class);

    @Autowired
    public DeveloperUsersFillerStartupRunner(UserInfoService userInfoService) {
        super(userInfoService);
        LOG.info("Dev database init");
    }

    protected void createUsers() {
        UserInfoDTO adminDTO = UserInfoDTOBuilder.getInstance()
                .login("admin")
                .password("password")
                .role(UserRole.ADMIN)
                .build();
        createUserOrResetPassword(getUserInfoService(), adminDTO);

        UserInfoDTO userDTO = UserInfoDTOBuilder.getInstance()
                .login("user")
                .password("useruser")
                .build();
        createUserOrResetPassword(getUserInfoService(), userDTO);

        UserInfoDTO deactivatedUserDTO = UserInfoDTOBuilder.getInstance()
                .login("nonActiveUser")
                .password("useruser")
                .role(UserRole.DEACTIVATED)
                .build();
        createUserOrResetPassword(getUserInfoService(), deactivatedUserDTO);

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

    void userInfoToLog(UserInfoDTO userInfoDTO) {
        LOG.info("         login   : " + userInfoDTO.getLogin());
        LOG.info("         password: " + userInfoDTO.getPassword());
        LOG.info("         role    : " + userInfoDTO.getRole().name());
    }
}
