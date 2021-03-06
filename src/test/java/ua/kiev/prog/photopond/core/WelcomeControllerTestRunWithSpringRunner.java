package ua.kiev.prog.photopond.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ua.kiev.prog.photopond.annotation.ImportSecurityConfiguration;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = WelcomeController.class)
@ImportSecurityConfiguration
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "unitTest", "securityWebAuthTestConfig", "test"})
class WelcomeControllerTestRunWithSpringRunner {

    @Autowired
    MockMvc mockMvc;

    private static final String ROOT_VIEW_NAME = "index";

    private static final String SERVER_ADDRESS = "https://localhost";

    @Test
    void rootUrlTest() throws Exception {
        matchViewNameAfterGetRequest("/", ROOT_VIEW_NAME);
    }

    @Test
    void indexWithoutSuffixUrlTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    @WithMockUser
    void indexWithoutSuffixUrlAndUserRoleTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void indexWithoutSuffixUrlAndAdminRoleTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    @WithMockUser(roles = "DEACTIVATED")
    void indexWithoutSuffixUrlAndDeactivatedRoleTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }


    @Test
    void indexWithSuffixTest() throws Exception {
        matchViewNameAfterGetRequest("/index.html", ROOT_VIEW_NAME);
    }

    @Test
    void indexWithBadSuffixUrlTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SERVER_ADDRESS + "/index.hhttmmll"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(SERVER_ADDRESS + "/login"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void about() throws Exception {
        matchViewNameAfterGetRequest("/about", "about");
    }

    @Test
    void privacyPolicy() throws Exception {
        matchViewNameAfterGetRequest("/public/privacyPolicy", "privacyPolicy");
    }


    @Test
    void terms() throws Exception {
        matchViewNameAfterGetRequest("/public/terms", "terms");
    }

    private void matchViewNameAfterGetRequest(String url, String viewName) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SERVER_ADDRESS + url))
                .andExpect(status().isOk())
                .andExpect(view().name(viewName));
    }
}
