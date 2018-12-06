package ua.kiev.prog.photopond.security;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ua.kiev.prog.photopond.annotation.ImportSecurityConfiguration;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LoginController.class)
@ImportSecurityConfiguration
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "securityWebAuthTestConfig"})
public class LoginControllerTest {
    private static final String SERVER_ADDRESS = "https://localhost";

    private static final String LOGIN_PROCESSING_URL = SERVER_ADDRESS + "/j_spring_security_check";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void loginFormTest() throws Exception {
        assertThat(mockMvc).isNotNull();
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(Matchers.containsString("j_login")))
                .andExpect(content().string(Matchers.containsString("j_password")))
                .andDo(print());
    }

    @Test
    public void requiresAuthentication() throws Exception {
        mockMvc
                .perform(get(SERVER_ADDRESS + "/user/user"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(SERVER_ADDRESS + "/login"))
                .andDo(print());
    }

    @Test
    public void authenticationFailed() throws Exception {
        mockMvc
                .perform(formLogin(LOGIN_PROCESSING_URL)
                        .user("j_login", "userTest")
                        .password("j_password", "invalid"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(unauthenticated())
                .andDo(print());
    }


    @Test
    public void authenticationSuccessTest() throws Exception {
        ResultActions requestResult = mockMvc.perform(formLogin(LOGIN_PROCESSING_URL)
                .user("j_login", "userTest")
                .password("j_password", "passwordTest"));


        requestResult.andExpect(status().isFound())
                .andExpect(redirectedUrl("/authorized.html"))
                .andExpect(authenticated().withUsername("userTest"));

    }

    @Test
    @WithMockUser("userR")
    public void redirectAfterSuccessAuthorization() throws Exception {
        mockMvc.perform(get(SERVER_ADDRESS + "/authorized.html"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userR/drive"));
    }

    @Test
    public void authorizationDeactivatedUser() throws Exception {
        mockMvc
                .perform(formLogin(LOGIN_PROCESSING_URL)
                        .user("j_login", "deactivatedUser")
                        .password("j_password", "qwerty123!"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(unauthenticated())
                .andDo(print());
    }

}

