package ua.kiev.prog.photopond.twitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;
import ua.kiev.prog.photopond.twitter.Exception.AssociateTwitterAccountException;
import ua.kiev.prog.photopond.twitter.Exception.DisassociateTwitterAccountException;
import ua.kiev.prog.photopond.twitter.Exception.TwitterAuthenticationException;
import ua.kiev.prog.photopond.twitter.Exception.TwitterControllerAdvice;

import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.twitter.TwitterConstants.ACCOUNT_VIEW_URL;
import static ua.kiev.prog.photopond.twitter.TwitterRequestMappingConstants.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {TwitterController.class, TwitterControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class,
        TwitterConstantsConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class TwitterControllerTest {

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
        login = "awesomeUser";
    }

    @Nested
    class AssociatedListView {
        @Test
        @WithMockUser(username = "awesomeUser")
        void success() throws Exception {
            //Given
            TwitterUserDTO twitterUserDTO = TwitterUserDTOBuilder.getInstance().id(7L).socialId(1234567L).name("twitterUser").build();
            when(twitterService.findAccountByLogin("awesomeUser")).thenReturn(twitterUserDTO);
            MockHttpServletRequestBuilder get = get(ACCOUNT_VIEW_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isOk())
                    .andExpect(view().name("twitter/associate"))
                    .andExpect(model().attribute("twitterUserDTO", twitterUserDTO));
            verify(twitterService, times(1)).findAccountByLogin("awesomeUser");
            verifyNoMoreInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void twitterUserNotFound() throws Exception {
            //Given
            when(twitterService.findAccountByLogin("awesomeUser")).thenReturn(null);
            MockHttpServletRequestBuilder get = get(ACCOUNT_VIEW_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isOk())
                    .andExpect(view().name("twitter/associate"))
                    .andExpect(model().attributeDoesNotExist("twitterUserDTO"));
            verify(twitterService, times(1)).findAccountByLogin("awesomeUser");
            verifyNoMoreInteractions(twitterService);
        }
    }

    @Nested
    class AssociatedList {
        @Test
        @WithMockUser(username = "awesomeUser")
        void success() throws Exception {
            //Given
            TwitterUserDTO twitterUserDTO = TwitterUserDTOBuilder.getInstance().id(7L).socialId(1234567L).name("twitterUser").build();
            when(twitterService.findAccountByLogin("awesomeUser")).thenReturn(twitterUserDTO);
            MockHttpServletRequestBuilder get = get(ACCOUNTS_LIST_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(7)))
                    .andExpect(jsonPath("socialId", is(1234567)))
                    .andExpect(jsonPath("name", is("twitterUser")));
            verify(twitterService, times(1)).findAccountByLogin("awesomeUser");
            verifyNoMoreInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void twitterUserNotFound() throws Exception {
            //Given
            when(twitterService.findAccountByLogin("awesomeUser")).thenReturn(null);
            MockHttpServletRequestBuilder get = get(ACCOUNTS_LIST_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isOk())
                    .andExpect(content().string(""));
            verify(twitterService, times(1)).findAccountByLogin("awesomeUser");
            verifyNoMoreInteractions(twitterService);
        }
    }

    @Nested
    class AssociateRedirect {
        @Test
        @WithMockUser(username = "awesomeUser")
        void success() throws Exception {
            //Given
            String redirectedUrl = "/redirected/url";
            when(twitterService.getAuthorizationUrl()).thenReturn(redirectedUrl);
            MockHttpServletRequestBuilder get = get(ASSOCIATE_ACCOUNT_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrl(redirectedUrl));
            verify(twitterService, times(1)).getAuthorizationUrl();
            verifyNoMoreInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void throwsException() throws Exception {
            //Given
            when(twitterService.getAuthorizationUrl()).thenThrow(AssociateTwitterAccountException.class);
            MockHttpServletRequestBuilder get = get(ASSOCIATE_ACCOUNT_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrl("/user/awesomeUser/settings"))
                    .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, "twitter.error.associate"));
            verify(twitterService, times(1)).getAuthorizationUrl();
            verifyNoMoreInteractions(twitterService);
        }
    }

    @Nested
    class AuthorizationRedirect {
        @Test
        @WithMockUser(username = "awesomeUser")
        void success() throws Exception {
            //Given
            String redirectedUrl = "/redirected/url";
            when(twitterService.getAuthenticationUrl()).thenReturn(redirectedUrl);
            MockHttpServletRequestBuilder get = get(AUTHENTICATION_WITH_TWITTER_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrl(redirectedUrl + "?userLogin=awesomeUser"));
            verify(twitterService, times(1)).getAuthenticationUrl();
            verifyNoMoreInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void throwsException() throws Exception {
            //Given
            when(twitterService.getAuthenticationUrl()).thenThrow(TwitterAuthenticationException.class);
            MockHttpServletRequestBuilder get = get(AUTHENTICATION_WITH_TWITTER_URL);

            //When
            ResultActions perform = mockMvc.perform(get);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrl("/login"))
                    .andExpect(flash().attribute(ERROR_AUTH_ATTRIBUTE_NAME, "twitter.error.authorization"));
            verify(twitterService, times(1)).getAuthenticationUrl();
            verifyNoMoreInteractions(twitterService);
        }
    }

    @Nested
    class DisassociateAccount {
        @Test
        @WithMockUser(username = "awesomeUser")
        void success() throws Exception {
            //Given
            MockHttpServletRequestBuilder post = post(DISASSOCIATE_ACCOUNT_URL);

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrl("/user/awesomeUser/settings"));
            verify(twitterService, times(1)).disassociateAccount("awesomeUser");
            verifyNoMoreInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void throwsException() throws Exception {
            //Given
            doThrow(DisassociateTwitterAccountException.class).when(twitterService).disassociateAccount(login);
            MockHttpServletRequestBuilder post = post(DISASSOCIATE_ACCOUNT_URL);

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrl("/user/awesomeUser/settings"))
                    .andExpect(flash().attribute(ERROR_ATTRIBUTE_NAME, "twitter.error.disassociate"));
            verify(twitterService, times(1)).disassociateAccount(login);
            verifyNoMoreInteractions(twitterService);
        }
    }
}