package ua.kiev.prog.photopond.twitter;

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
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;
import ua.kiev.prog.photopond.twitter.exception.AssociateTwitterAccountException;
import ua.kiev.prog.photopond.twitter.exception.TwitterAccountAlreadyAssociateException;
import ua.kiev.prog.photopond.twitter.exception.TwitterAuthenticationException;
import ua.kiev.prog.photopond.twitter.exception.TwitterControllerAdvice;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.twitter.TwitterRequestMappingConstants.*;
import static ua.kiev.prog.photopond.user.UserRole.USER;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {TwitterCallbackController.class, TwitterControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class,
        TwitterConstantsConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class TwitterCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TwitterService twitterService;

    @MockBean
    private BindingErrorResolver bindingErrorResolver;

    private String login;

    @BeforeEach
    void setUp() {
        reset(twitterService, bindingErrorResolver);
        when(bindingErrorResolver.resolveMessage(any(String.class), any(Locale.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        login = "userLogin";
    }

    @Test
    @WithMockUser(username = "userLogin")
    void associateAccount() throws Exception {
        //Given
        TwitterUserDTO twitterUserDTO = TwitterUserDTOBuilder.getInstance().id(7L).socialId(1234567L).name("screenName").build();
        when(twitterService.associateAccount("userLogin", "oauth-token", "oauth-verifier")).thenReturn(twitterUserDTO);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ASSOCIATE_CALLBACK_SHORT_URL)
                .param("oauth_token", "oauth-token")
                .param("oauth_verifier", "oauth-verifier");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"));
        verify(twitterService, times(1)).associateAccount("userLogin", "oauth-token", "oauth-verifier");
        verifyNoMoreInteractions(twitterService);
    }

    static Stream<Arguments> associateExceptionStream() {
        return Stream.of(
                Arguments.of(TwitterAccountAlreadyAssociateException.class, "twitter.error.accountAlreadyAssociate"),
                Arguments.of(AssociateTwitterAccountException.class, "twitter.error.associate")
        );
    }

    @ParameterizedTest(name = "[{index}] ==> {0}")
    @MethodSource(value = {"associateExceptionStream"})
    @WithMockUser(username = "userLogin")
    void associateAccountServiceThrowsException(Class<Throwable> clazz, String message) throws Exception {
        //Given
        when(twitterService.associateAccount("userLogin", "oauth-token", "oauth-verifier")).thenThrow(clazz);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ASSOCIATE_CALLBACK_SHORT_URL)
                .param("oauth_token", "oauth-token")
                .param("oauth_verifier", "oauth-verifier");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, message));
        verify(twitterService, times(1)).associateAccount("userLogin", "oauth-token", "oauth-verifier");
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    @WithAnonymousUser
    void associateAccountWithUnauthorizedUser() {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ASSOCIATE_CALLBACK_SHORT_URL)
                .param("oauth_token", "oauth-token")
                .param("oauth_verifier", "oauth-verifier");

        //When
        NestedServletException nestedServletException = assertThrows(NestedServletException.class, () -> mockMvc.perform(get));

        //Then
        verify(bindingErrorResolver, times(1)).resolveMessage(eq("twitter.error.unauthorizedAssociation"), any());
        assertThat(nestedServletException).hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @WithMockUser(username = "userLogin")
    void associateAccountError() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(ASSOCIATE_CALLBACK_SHORT_URL)
                .param("denied", "oauth-token");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/userLogin/settings"))
                .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, "twitter.error.associate"));
        verify(twitterService, times(1)).removeRequestToken("oauth-token");
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    void authorization() throws Exception {
        //Given
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(7L).login("awesomeUser").password("password").role(USER)
                .build();
        when(twitterService.findUserInfoByRequestToken("oauth-token", "oauth-verifier")).thenReturn(userInfoDTO);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(LOGIN_CALLBACK_SHORT_URL)
                .param("oauth_token", "oauth-token")
                .param("oauth_verifier", "oauth-verifier");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/awesomeUser/drive"));
        verify(twitterService, times(1)).findUserInfoByRequestToken("oauth-token", "oauth-verifier");
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    void authorizationServiceThrowsException() throws Exception {
        //Given
        when(twitterService.findUserInfoByRequestToken("oauth-token", "oauth-verifier"))
                .thenThrow(TwitterAuthenticationException.class);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(LOGIN_CALLBACK_SHORT_URL)
                .param("oauth_token", "oauth-token")
                .param("oauth_verifier", "oauth-verifier");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(ERROR_AUTH_ATTRIBUTE_NAME, "twitter.error.authorization"));
        verify(twitterService, times(1)).findUserInfoByRequestToken("oauth-token", "oauth-verifier");
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    void authorizationError() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(LOGIN_CALLBACK_SHORT_URL)
                .param("denied", "oauth-token");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(ERROR_AUTH_ATTRIBUTE_NAME, "twitter.error.authorization"));
        verify(twitterService).removeRequestToken("oauth-token");
        verifyNoMoreInteractions(twitterService);
    }
}