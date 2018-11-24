package ua.kiev.prog.photopond.user.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

public abstract class AbstractUsersFillerStartupRunner implements CommandLineRunner {

    private static final Logger LOG = LogManager.getLogger(AbstractUsersFillerStartupRunner.class);

    private final UserInfoService userInfoService;

    public AbstractUsersFillerStartupRunner(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    public UserInfoService getUserInfoService() {
        return userInfoService;
    }

    @Override
    public final void run(String... args) {
        createUsers();
    }

    public void createUsers() {
        createUnmodifiableUsers();
        createModifiableUsers();
    }

    protected abstract void createModifiableUsers();

    private void createUnmodifiableUsers() {
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
        printTable("Unmodifiable users", adminDTO, userDTO, deactivatedUserDTO);
    }

    protected void printTable(String caption, UserInfoDTO ... users) {
        LOG.info("    =======================================");
        LOG.info("                 " + caption);
        LOG.info("    =======================================");
        for (int i = 0; i < users.length; i++) {
            if (i != 0) {
                LOG.info("    ---------------------------------------");
            }
            userInfoToLog(users[i]);
        }
        LOG.info("    =======================================");
    }

    protected void userInfoToLog(UserInfoDTO userInfoDTO) {
        LOG.info("         login   : " + userInfoDTO.getLogin());
        LOG.info("         password: " + userInfoDTO.getPassword());
        LOG.info("         role    : " + userInfoDTO.getRole().name());
    }

    protected void createUserOrResetPassword(UserInfoService userInfoService, UserInfoDTO userDTO) {
        if (userInfoService.existsByLogin(userDTO.getLogin())) {
            userInfoService.resetPassword(userDTO.getLogin(), userDTO.getPassword());
        } else {
            userInfoService.addUser(userDTO);
        }
        userInfoService.findUserByLogin(userDTO.getLogin())
                .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve " + userDTO.getLogin() + " user"));
    }
}
