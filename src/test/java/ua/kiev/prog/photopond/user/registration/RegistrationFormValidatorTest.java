package ua.kiev.prog.photopond.user.registration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationFormValidatorTest {
    private Validator validatorUnderTest;

    @Mock
    private UserInfoService userInfoService;

    private RegistrationForm form;
    private UserInfo userInfo;
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        when(userInfoService.existByLogin(any(String.class))).thenReturn(false);
        validatorUnderTest = new RegistrationFormValidator(userInfoService);
        initRegistrationForm();

        errors = new BeanPropertyBindingResult(form, "form");
    }

    private void initRegistrationForm() {
        form = new RegistrationForm();
        userInfo = new UserInfo("simpleUser", "qwerty");
        form.setUserInfo(userInfo);
        form.setPasswordConfirmation(userInfo.getPassword());
    }

    @Test
    public void successValidation() throws Exception {
        ValidationUtils.invokeValidator(validatorUnderTest, form, errors);

        verify(userInfoService).existByLogin(userInfo.getLogin());
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    public void nullRegistrationForm() throws Exception {
        ValidationUtils.invokeValidator(validatorUnderTest, null, errors);

        verifyZeroInteractions(userInfoService);
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
    }

    @Test
    public void userInfoIsNull() throws Exception {
        form.setUserInfo(null);

        ValidationUtils.invokeValidator(validatorUnderTest, form, errors);

        verifyZeroInteractions(userInfoService);
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(2);
        assertThat(errors.getFieldError("userInfo.login")).isNotNull();
        assertThat(errors.getFieldError("userInfo.password")).isNotNull();
    }

    @Test
    public void loginAlreadyExists() throws Exception {
        when(userInfoService.existByLogin(userInfo.getLogin()))
                .thenReturn(true);

        ValidationUtils.invokeValidator(validatorUnderTest, form, errors);

        verify(userInfoService).existByLogin(userInfo.getLogin());
        verifyNoMoreInteractions(userInfoService);
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(errors.getFieldError("userInfo.login")).isNotNull();
    }

    @Test
    public void wrongPasswordConfirmation() throws Exception {
        form.setPasswordConfirmation("!" + userInfo.getPassword());

        ValidationUtils.invokeValidator(validatorUnderTest, form, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(errors.getFieldError("passwordConfirmation")).isNotNull();
    }

    @Test
    public void testSupportedClass() throws Exception {
        boolean support = validatorUnderTest.supports(form.getClass());

        assertThat(support).isTrue();
    }

    @Test
    public void testNotSupportedClass() throws Exception {
        boolean support = validatorUnderTest.supports(Object.class);

        assertThat(support).isFalse();
    }
}