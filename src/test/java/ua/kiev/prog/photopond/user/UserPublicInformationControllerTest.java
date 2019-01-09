package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserPublicInformationController.class)
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class UserPublicInformationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInfoService userInfoService;

    @Test
    void retrieveUserAvatar() throws Exception {
        //Given
        String login = "UserLogin";
        when(userInfoService.retrieveAvatar(login)).thenReturn(login.getBytes());
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/public/user/{login}/avatar", login);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform
                .andExpect(status().isOk())
                .andExpect(content().bytes(login.getBytes()));
        verify(userInfoService, times(1)).retrieveAvatar(login);
        verifyNoMoreInteractions(userInfoService);
    }
}