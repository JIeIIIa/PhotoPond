package ua.kiev.prog.photopond.facebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.facebook.exception.DisassociateFBAccountException;
import ua.kiev.prog.photopond.facebook.exception.FBControllerAdvice;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {FBController.class, FBControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        FBControllerTestConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class FBControllerTest {

    @MockBean
    private FBService fbService;

    @Autowired
    private BindingErrorResolver bindingErrorResolver;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "userLogin")
    void associatedListView() throws Exception {
        //Given
        FBUserDTO fbUserDTO = FBUserDTOBuilder.getInstance().fbId("1234567").name("fbName").email("email@email.com").build();
        when(fbService.findAccountByLogin("userLogin")).thenReturn(fbUserDTO);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ACCOUNT_VIEW_URL);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isOk())
                .andExpect(view().name("facebook/associate"))
                .andExpect(model().attribute("fbUserDTO", fbUserDTO));
        verify(fbService, times(1)).findAccountByLogin("userLogin");
        verifyNoMoreInteractions(fbService);
    }

    @Test
    @WithMockUser(username = "userLogin")
    void associatedList() throws Exception {
        //Given
        FBUserDTO fbUserDTO = FBUserDTOBuilder.getInstance().fbId("1234567").name("fbName").email("email@email.com").build();
        when(fbService.findAccountByLogin("userLogin")).thenReturn(fbUserDTO);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ACCOUNTS_LIST_URL);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("fbId", is("1234567")))
                .andExpect(jsonPath("name", is("fbName")))
                .andExpect(jsonPath("email", is("email@email.com")));
        verify(fbService, times(1)).findAccountByLogin("userLogin");
        verifyNoMoreInteractions(fbService);

    }

    @Test
    @WithMockUser(username = "userLogin")
    void disassociateAccount() throws Exception {
        //Given
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(DISASSOCIATE_ACCOUNT_URL);

        //When
        ResultActions perform = mockMvc.perform(post);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"));
        verify(fbService, times(1)).disassociateAccount("userLogin");
        verifyNoMoreInteractions(fbService);
    }

    @Test
    @WithMockUser(username = "userLogin")
    void disassociateAccountThrowsDisassociateFBAccountException() throws Exception {
        //Given
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(DISASSOCIATE_ACCOUNT_URL);
        doThrow(DisassociateFBAccountException.class).when(fbService).disassociateAccount(any());
        when(bindingErrorResolver.resolveMessage(any(String.class), any())).thenReturn("errorMessage");

        //When
        ResultActions perform = mockMvc.perform(post);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, "errorMessage"));
        verify(fbService, times(1)).disassociateAccount("userLogin");
        verifyNoMoreInteractions(fbService);
    }

    @Test
    @WithMockUser(username = "userLogin")
    void associateRedirect() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ASSOCIATE_ACCOUNT_URL);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("https://www.facebook.com/**/**state=ASSOCIATE**"));
    }

    @Test
    void authorizationRedirect() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(AUTHENTICATION_WITH_FACEBOOK_URL);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("https://www.facebook.com/**/**state=LOGIN**"));
    }
}