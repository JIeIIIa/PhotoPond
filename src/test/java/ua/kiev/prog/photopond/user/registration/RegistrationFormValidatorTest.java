package ua.kiev.prog.photopond.user.registration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoSimpleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class RegistrationFormValidatorTest {
    private Validator validatorUnderTest;

    @MockBean
    private UserInfoSimpleRepository userInfoSimpleRepository;

    private RegistrationForm form;
    private UserInfo userInfo;
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(userInfoSimpleRepository.existByLogin(any(String.class))).thenReturn(false);
        validatorUnderTest = new RegistrationFormValidator(userInfoSimpleRepository);
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

        verify(userInfoSimpleRepository).existByLogin(userInfo.getLogin());
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    public void nullRegistrationForm() throws Exception {
        ValidationUtils.invokeValidator(validatorUnderTest, null, errors);

        verifyZeroInteractions(userInfoSimpleRepository);
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(1);
    }

    @Test
    public void userInfoIsNull() throws Exception {
        form.setUserInfo(null);

        ValidationUtils.invokeValidator(validatorUnderTest, form, errors);

        verifyZeroInteractions(userInfoSimpleRepository);
        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getErrorCount()).isEqualTo(2);
        assertThat(errors.getFieldError("userInfo.login")).isNotNull();
        assertThat(errors.getFieldError("userInfo.password")).isNotNull();
    }

    @Test
    public void loginAlreadyExists() throws Exception {
        when(userInfoSimpleRepository.existByLogin(userInfo.getLogin()))
                .thenReturn(true);

        ValidationUtils.invokeValidator(validatorUnderTest, form, errors);

        verify(userInfoSimpleRepository).existByLogin(userInfo.getLogin());
        verifyNoMoreInteractions(userInfoSimpleRepository);
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