package ua.kiev.prog.photopond.user.registration;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ua.kiev.prog.photopond.user.UserInfo;

@Component
public class RegistrationFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return RegistrationForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RegistrationForm form = (RegistrationForm) o;
        String password = getPassword(form.getUserInfo());
        String passwordConfirmation = form.getPasswordConfirmation();

        if ((password == null && passwordConfirmation != null) ||
                (password != null && !password.equals(passwordConfirmation))) {
            errors.rejectValue("passwordConfirmation", "notEquals", "Password and confirmation aren't equals");
        }


    }

    private String getPassword(UserInfo user) {
        String password;

        if (user == null) {
            password = null;
        } else {
            password = user.getPassword();
        }

        return password;
    }
}
