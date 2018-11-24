package ua.kiev.prog.photopond.user.Configuration;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import ua.kiev.prog.photopond.annotation.profile.Prod;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

@Configuration
@Order(value = Integer.MAX_VALUE - 1000)
@Prod
public class ProductionUsersFillerStartupRunner extends AbstractUsersFillerStartupRunner {
    private static final Logger LOG = LogManager.getLogger(ProductionUsersFillerStartupRunner.class);

    @Autowired
    public ProductionUsersFillerStartupRunner(UserInfoService userInfoService) {
        super(userInfoService);
        LOG.info("Prod database init");
    }

    protected void createModifiableUsers() {
        String password = generatePassword();
        String adminLogin = "PhotoPondSuperAdmin";
        UserInfoDTO adminDTO  = UserInfoDTOBuilder.getInstance()
                .login(adminLogin)
                .password(password)
                .role(UserRole.ADMIN)
                .build();

        createUserOrResetPassword(getUserInfoService(), adminDTO);

        LOG.info("    =======================================");
        LOG.info("              Default administrators ");
        LOG.info("    =======================================");
        LOG.info("         login   : " + adminDTO.getLogin());
        LOG.info("         password: " + password);
        LOG.info("    =======================================");
    }

    private String generatePassword() {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                .filteredBy(CharacterPredicates.LETTERS)
                .build();
        return randomStringGenerator.generate(10, 20);
    }
}
