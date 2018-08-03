package ua.kiev.prog.photopond.user;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class EqualPasswordsValidator implements ConstraintValidator<EqualPasswords, ConfirmedPassword> {

    @Override
    public void initialize(EqualPasswords constraintAnnotation) {
    }

    @Override
    public boolean isValid(ConfirmedPassword confirmedPassword, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.equals(confirmedPassword.getPassword(), confirmedPassword.getPasswordConfirmation());
    }
}
