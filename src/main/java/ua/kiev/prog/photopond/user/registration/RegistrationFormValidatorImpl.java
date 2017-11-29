package ua.kiev.prog.photopond.user.registration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

@Component
public class RegistrationFormValidatorImpl implements RegistrationFormValidator{
    private static Logger log = LogManager.getLogger(RegistrationFormValidatorImpl.class);
    private final UserInfoService userInfoService;

    @Autowired
    public RegistrationFormValidatorImpl(UserInfoService userInfoService) {
        log.debug("Create instance of RegistrationFormValidatorImpl.class");
        this.userInfoService = userInfoService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return RegistrationForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if (o == null) {
            errors.rejectValue("userInfo", "Errors");
            return;
        }

        RegistrationForm form = (RegistrationForm) o;
        UserInfo user = form.getUserInfo();
        log.trace("Validate " + user);
        String login = null;
        String password = null;

        if (user == null) {
            errors.rejectValue("userInfo.login", "NotNull.userInfo.login", "Incorrect login size");
            errors.rejectValue("userInfo.password", "NotNull.userInfo.password", "Incorrect password size");
        } else {
            login = user.getLogin();
            password = user.getPassword();
        }
        String passwordConfirmation = form.getPasswordConfirmation();

        if (login != null && userInfoService.existByLogin(login)) {
            errors.rejectValue("userInfo.login", "RegistrationForm.loginExists", "This login exists. Please, choose another login.");
        }

        if (password != null && !password.equals(passwordConfirmation)) {
            errors.rejectValue("passwordConfirmation", "RegistrationForm.passwordConfirmation", "Password and confirmation aren't equals");
        }
        log.trace("Validate " + user + ":   has errors = " + errors.hasErrors());
    }
}
