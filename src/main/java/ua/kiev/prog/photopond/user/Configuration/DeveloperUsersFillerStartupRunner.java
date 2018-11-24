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

    protected void createModifiableUsers() {
        UserInfoDTO moderDTO = UserInfoDTOBuilder.getInstance()
                .login("Moder")
                .password("password")
                .role(UserRole.ADMIN)
                .build();
        createUserOrResetPassword(getUserInfoService(), moderDTO);

        UserInfoDTO uzerDTO = UserInfoDTOBuilder.getInstance()
                .login("Uzzzer")
                .password("password")
                .build();
        createUserOrResetPassword(getUserInfoService(), uzerDTO);

        UserInfoDTO nonActiveDTO = UserInfoDTOBuilder.getInstance()
                .login("nonActive")
                .password("password")
                .role(UserRole.DEACTIVATED)
                .build();
        createUserOrResetPassword(getUserInfoService(), nonActiveDTO);

        printTable("Modifiable users", moderDTO, uzerDTO, nonActiveDTO);
    }
}
