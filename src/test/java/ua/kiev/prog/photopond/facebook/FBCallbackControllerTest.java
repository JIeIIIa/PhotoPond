package ua.kiev.prog.photopond.facebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.facebook.exception.AssociateFBAccountException;
import ua.kiev.prog.photopond.facebook.exception.FBAccountAlreadyAssociateException;
import ua.kiev.prog.photopond.facebook.exception.FBAuthenticationException;
import ua.kiev.prog.photopond.facebook.exception.FBControllerAdvice;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.facebook.FBConstants.FB_CALLBACK_URL;
import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ERROR_ATTRIBUTE_NAME;
import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ERROR_AUTH_ATTRIBUTE_NAME;
import static ua.kiev.prog.photopond.user.UserRole.USER;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {FBCallbackController.class, FBControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        FBControllerTestConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class FBCallbackControllerTest {

    @MockBean
    private FBService fbService;

    @Autowired
    private BindingErrorResolver bindingErrorResolver;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(fbService, bindingErrorResolver);
        when(bindingErrorResolver.resolveMessage(any(String.class), any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    @Test
    @WithMockUser(username = "userLogin")
    void associateAccount() throws Exception {
        //Given
        FBUserDTO fbUserDTO = FBUserDTOBuilder.getInstance().fbId("1234567").name("fbName").email("email@email.com").build();
        when(fbService.associateAccount("userLogin", "code")).thenReturn(fbUserDTO);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "ASSOCIATE")
                .param("code", "code");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"));
        verify(fbService, times(1)).associateAccount("userLogin", "code");
        verifyNoMoreInteractions(fbService);
    }

    static Stream<Arguments> associateExceptionStream() {
        return Stream.of(
                Arguments.of(FBAccountAlreadyAssociateException.class, "facebook.error.accountAlreadyAssociate"),
                Arguments.of(AssociateFBAccountException.class, "facebook.error.associate")
        );
    }

    @ParameterizedTest(name = "[{index}] ==> {0}")
    @MethodSource(value = {"associateExceptionStream"})
    @WithMockUser(username = "userLogin")
    void associateAccountServiceThrowsException(Class<Throwable> clazz, String message) throws Exception {
        //Given
        when(fbService.associateAccount("userLogin", "code")).thenThrow(clazz);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "ASSOCIATE")
                .param("code", "code");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, message));
        verify(fbService, times(1)).associateAccount("userLogin", "code");
        verifyNoMoreInteractions(fbService);
    }

    @Test
    @WithAnonymousUser
    void associateAccountWithUnauthorizedUser() {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "ASSOCIATE")
                .param("code", "code");

        //When
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> mockMvc.perform(get));

        //Then
        verify(bindingErrorResolver, times(1)).resolveMessage(eq("facebook.error.unauthorizedAssociation"), any());
        assertThat(nestedServletException).hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @WithMockUser(username = "userLogin")
    void associateAccountError() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "ASSOCIATE")
                .param("error", "error");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, "facebook.error.associate"));
        verifyNoMoreInteractions(fbService);
    }

    @Test
    void authorization() throws Exception {
        //Given
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(7L).login("awesomeUser").password("password").role(USER)
                .build();
        when(fbService.findUserInfoByCode("facebook-code")).thenReturn(userInfoDTO);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "LOGIN")
                .param("code", "facebook-code");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/awesomeUser/drive"));
        verify(fbService, times(1)).findUserInfoByCode("facebook-code");
        verifyNoMoreInteractions(fbService);
    }

    @Test
    void authorizationServiceThrowsException() throws Exception {
        //Given
        when(fbService.findUserInfoByCode("facebook-code")).thenThrow(FBAuthenticationException.class);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "LOGIN")
                .param("code", "facebook-code");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(ERROR_AUTH_ATTRIBUTE_NAME, "facebook.error.authorization"));
        verify(fbService, times(1)).findUserInfoByCode("facebook-code");
        verifyNoMoreInteractions(fbService);
    }

    @Test
    void authorizationError() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("state", "LOGIN")
                .param("error", "error");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(ERROR_AUTH_ATTRIBUTE_NAME, "facebook.error.authorization"));
        verifyNoMoreInteractions(fbService);
    }

    @Test
    void error() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(FB_CALLBACK_URL)
                .param("error", "error");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isOk())
                .andExpect(view().name("errors/commonError"))
                .andExpect(model().attributeExists("status", "error", "trace"));
        verifyNoMoreInteractions(fbService);
    }
}