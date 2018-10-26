package ua.kiev.prog.photopond.user.Configuration;

import org.springframework.boot.CommandLineRunner;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoService;

public abstract class AbstractUsersFillerStartupRunner implements CommandLineRunner {

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

    protected abstract void createUsers();

    void createUserOrResetPassword(UserInfoService userInfoService, UserInfoDTO userDTO) {
        if (userInfoService.existsByLogin(userDTO.getLogin())) {
            userInfoService.resetPassword(userDTO.getLogin(), userDTO.getPassword());
        } else {
            userInfoService.addUser(userDTO);
        }
        userInfoService.findUserByLogin(userDTO.getLogin())
                .orElseThrow(() -> new ExceptionInInitializerError("Failure retrieve " + userDTO.getLogin() + " user"));
    }
}
