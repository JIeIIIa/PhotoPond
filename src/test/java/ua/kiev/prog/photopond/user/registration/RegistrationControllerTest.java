package ua.kiev.prog.photopond.user.registration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ua.kiev.prog.photopond.annotation.ImportSecurityConfiguration;
import ua.kiev.prog.photopond.configuration.UserInfoServiceMockConfiguration;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.user.registration.RegistrationController.REGISTRATION_FORM_NAME;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RegistrationController.class)
@ImportSecurityConfiguration
@ContextConfiguration(classes = {
        UserInfoServiceMockConfiguration.class,
        WebMvcTestContextConfiguration.class,
        RegistrationControllerTestConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})

@ActiveProfiles({"dev", "unitTest", "securityWebAuthTestConfig"})
public class RegistrationControllerTest {
    private static final String REGISTRATION_URL = "/registration";
    private static final String REGISTRATION_MODEL_NAME = "registration";

    private static final String LOGIN_ATTRIBUTE_NAME = "userInfo.login";
    private static final String PASSWORD_ATTRIBUTE_NAME = "userInfo.password";
    private static final String PASSWORD_CONFIRMATION_ATTRIBUTE_NAME = "passwordConfirmation";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserInfoService userInfoService;

    @Test
    public void availableRegistrationPageTest() throws Exception {
        mockMvc.perform(get(REGISTRATION_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    @Test
    public void sendCorrectData() throws Exception {
        UserInfo targetUser = new UserInfo("newUser", "somePassword");
        MockHttpServletRequestBuilder requestBuilder = post(REGISTRATION_URL)
                .param(LOGIN_ATTRIBUTE_NAME, targetUser.getLogin())
                .param(PASSWORD_ATTRIBUTE_NAME, targetUser.getPassword())
                .param(PASSWORD_CONFIRMATION_ATTRIBUTE_NAME, targetUser.getPassword())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/" + targetUser.getLogin()));

        ArgumentCaptor<UserInfoDTO> argument = ArgumentCaptor.forClass(UserInfoDTO.class);
        verify(userInfoService).addUser(argument.capture());
        UserInfoDTO createdUser = argument.getValue();
        assertThat(createdUser.getLogin()).isEqualTo(targetUser.getLogin());
        assertThat(createdUser.getPassword()).isEqualTo(targetUser.getPassword());
        assertThat(createdUser.getRole()).isEqualTo(UserRole.USER);
    }

    private void failureUserRegistration(MockHttpServletRequestBuilder post) throws Exception {
        mockMvc.perform(post)
                .andExpect(status().isOk())
                .andExpect(view().name(REGISTRATION_MODEL_NAME))
                .andDo(print())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists(REGISTRATION_FORM_NAME))
                .andExpect(model().attributeHasErrors(REGISTRATION_FORM_NAME));
    }

    @Test
    public void nullLoginAndPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL);

        failureUserRegistration(post);
    }

    @Test
    public void nullLoginAndCorrectPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param(PASSWORD_ATTRIBUTE_NAME, "qwerty123");

        failureUserRegistration(post);
    }

    @Test
    public void smallLoginAndCorrectPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param(LOGIN_ATTRIBUTE_NAME, "use")
                .param(PASSWORD_ATTRIBUTE_NAME, "qwerty123");

        failureUserRegistration(post);
    }


    @Test
    public void largeLoginAndCorrectPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param(LOGIN_ATTRIBUTE_NAME, "0user1user2user3user4user5user6user7user8user9user")
                .param(PASSWORD_ATTRIBUTE_NAME, "qwerty123");

        failureUserRegistration(post);
    }

    @Test
    public void correctLoginAndSmallPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param(LOGIN_ATTRIBUTE_NAME, "someUser")
                .param(PASSWORD_ATTRIBUTE_NAME, "pas");

        failureUserRegistration(post);
    }

    @Test
    public void correctLoginAndLargePassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param(LOGIN_ATTRIBUTE_NAME, "someUser")
                .param(PASSWORD_ATTRIBUTE_NAME, "0password1password2password3password");

        failureUserRegistration(post);
    }

    @Test
    public void correctLoginWrongPasswordConfirmation() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param(LOGIN_ATTRIBUTE_NAME, "someUser")
                .param(PASSWORD_ATTRIBUTE_NAME, "password")
                .param(PASSWORD_CONFIRMATION_ATTRIBUTE_NAME, "anotherPassword");

        failureUserRegistration(post);
    }
}