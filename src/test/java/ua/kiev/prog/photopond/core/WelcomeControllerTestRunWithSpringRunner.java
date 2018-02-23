package ua.kiev.prog.photopond.core;

import groovy.util.logging.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ua.kiev.prog.photopond.annotation.ImportSecurityConfiguration;
import ua.kiev.prog.photopond.configuration.UserInfoServiceMockConfiguration;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;
import ua.kiev.prog.photopond.user.UserInfoService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = WelcomeController.class)
@ImportSecurityConfiguration
@ContextConfiguration(classes = {
        UserInfoServiceMockConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({"dev", "unitTest", "securityWebAuthTestConfig"})
@Log4j2
public class WelcomeControllerTestRunWithSpringRunner {
    @Autowired
    UserInfoService userInfoService;

    @Autowired
    MockMvc mockMvc;

    private static final String ROOT_VIEW_NAME = "index";

    @Test
    public void rootUrlTest() throws Exception {
        matchViewNameAfterGetRequest("/", ROOT_VIEW_NAME);
    }

    @Test
    public void indexWithoutSuffixUrlTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    @WithMockUser
    public void indexWithoutSuffixUrlAndUserRoleTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void indexWithoutSuffixUrlAndAdminRoleTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    @WithMockUser(roles = "DEACTIVATED")
    public void indexWithoutSuffixUrlAndDeactivatedRoleTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }



    @Test
    public void indexWithSuffixTest() throws Exception {
        matchViewNameAfterGetRequest("/index.html", ROOT_VIEW_NAME);
    }

    @Test
    public void indexWithBadSuffixUrlTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("index.hhttmmll"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("http:/*/login"))
                .andDo(MockMvcResultHandlers.print());
    }

    private void matchViewNameAfterGetRequest(String url, String viewName) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(viewName));
    }
}