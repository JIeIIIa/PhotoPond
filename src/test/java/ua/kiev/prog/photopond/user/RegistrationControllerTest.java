package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RegistrationController.class)
@ContextConfiguration(classes = SpringSecurityWebAuthenticationTestConfiguration.class)
@AutoConfigureMockMvc
public class RegistrationControllerTest {
    private static final String REGISTRATION_URL = "/registration";
    public static final String REGISTRATION_MODEL_NAME = "registration";
    public static final String USER_INFO_ATTRIBUTE_NAME = "userInfo";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInfoSimpleRepository userInfoSimpleRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

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
                .param("login", targetUser.getLogin())
                .param("password", targetUser.getPassword())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        ArgumentCaptor<UserInfo> argument = ArgumentCaptor.forClass(UserInfo.class);
        verify(userInfoSimpleRepository).addUser(argument.capture());
        UserInfo createdUser = argument.getValue();
        assertThat(createdUser).isEqualTo(targetUser);
        assertThat(createdUser.getRole()).isEqualTo(UserRole.USER);
    }

    private void failureUserRegistration(MockHttpServletRequestBuilder post) throws Exception {
        mockMvc.perform(post)
                .andExpect(status().isOk())
                .andExpect(view().name(REGISTRATION_MODEL_NAME))
                .andDo(print())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists(USER_INFO_ATTRIBUTE_NAME))
                .andExpect(model().attributeHasErrors(USER_INFO_ATTRIBUTE_NAME));
    }

    @Test
    public void nullLoginAndPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL);

        failureUserRegistration(post);
    }

    @Test
    public void nullLoginAndCorrectPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL).param("password", "qwerty123");

        failureUserRegistration(post);
    }

    @Test
    public void smallLoginAndCorrectPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param("login", "use")
                .param("password", "qwerty123");

        failureUserRegistration(post);
    }


    @Test
    public void largeLoginAndCorrectPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param("login", "0user1user2user3user4user5user6user7user8user9user")
                .param("password", "qwerty123");

        failureUserRegistration(post);
    }

    @Test
    public void correctLoginAndSmallPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param("login", "someUser")
                .param("password", "pas");

        failureUserRegistration(post);
    }

    @Test
    public void correctLoginAndLargePassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param("login", "someUser")
                .param("password", "0password1password2password3password");

        failureUserRegistration(post);
    }

    @Test
    public void correctLoginAndPassword() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL)
                .param("login", "someUser")
                .param("password", "password");

        mockMvc.perform(post)
                .andExpect(status().isOk())
                .andExpect(view().name("Success"))
                .andDo(print());
    }

}