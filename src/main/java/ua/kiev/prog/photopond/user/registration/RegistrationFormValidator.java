package ua.kiev.prog.photopond.user.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

@Component
public class RegistrationFormValidator implements Validator {
    private final UserInfoService userInfoService;

    @Autowired
    public RegistrationFormValidator(UserInfoService userInfoService) {
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

    }

}
