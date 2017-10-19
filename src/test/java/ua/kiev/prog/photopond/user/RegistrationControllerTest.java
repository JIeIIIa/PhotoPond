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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RegistrationController.class)
@ContextConfiguration(classes = SpringSecurityWebAuthenticationTestConfiguration.class)
@AutoConfigureMockMvc
public class RegistrationControllerTest {
    private static final String REGISTRATION_URL = "/registration";
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
                .andExpect(MockMvcResultMatchers.view().name("registration"))
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

    @Test
    public void nullUserLogin() throws Exception {
        MockHttpServletRequestBuilder post = post(REGISTRATION_URL);
        mockMvc.perform(post)
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().string(containsString("Login Error")))
                .andExpect(content().string(containsString("Password Error")));


    }
}