package ua.kiev.prog.photopond.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.transfer.ChangePassword;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;


@ExtendWith(SpringExtension.class)
public class EqualPasswordsValidatorTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void validPasswordAndConfirmation() {
        //Given
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(777L)
                .login("userLogin")
                .oldPassword("oldPassword")
                .password("qwerty123!")
                .passwordConfirmation("qwerty123!")
                .role(UserRole.ADMIN)
                .build();

        //When
        Set<ConstraintViolation<UserInfoDTO>> constraintViolations = validator.validate(userInfoDTO, ChangePassword.class);

        //Then
        Assertions.assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void notEqualsPasswordAndConfirmation() {
        //Given
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(777L)
                .login("userLogin")
                .oldPassword("oldPassword")
                .password("qwerty123!")
                .passwordConfirmation("anotherPassword")
                .role(UserRole.ADMIN)
                .build();

        //When
        Set<ConstraintViolation<UserInfoDTO>> constraintViolations = validator.validate(userInfoDTO, ChangePassword.class);

        //Then
        Assertions.assertThat(constraintViolations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                     .containsExactly("{EqualPasswords}");
    }
}