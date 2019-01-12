package ua.kiev.prog.photopond.administration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {AdministrationController.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class AdministrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administrationPanelView() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = get("/administration/adminPanel");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isOk())
                .andExpect(view().name("administration/administration"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void administrationPanelViewForUser() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = get("/administration/adminPanel");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/accessDenied.html"));
    }
}