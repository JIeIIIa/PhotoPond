package ua.kiev.prog.photopond.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Service
public class UniqueLoginValidator implements ConstraintValidator<UniqueLogin, String> {

    private final UserInfoService userInfoService;

    @Autowired
    public UniqueLoginValidator(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    public void initialize(UniqueLogin constraint) {
    }

    public boolean isValid(String login, ConstraintValidatorContext context) {
        return userInfoService.existsByLogin(login);
    }
}
