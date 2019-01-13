package ua.kiev.prog.photopond.twitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import ua.kiev.prog.photopond.twitter.exception.NotFoundTwitterAssociatedAccountException;
import ua.kiev.prog.photopond.twitter.exception.TweetPublishingException;
import ua.kiev.prog.photopond.twitter.exception.TwitterControllerAdvice;

import java.util.Locale;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.Utils.TestUtils.convertObjectToJsonBytes;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {TwitterApiController.class, TwitterControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class TwitterApiControllerTest {

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
    class Tweet {
        @Test
        @WithMockUser(username = "awesomeUser")
        void success() throws Exception {
            //Given
            TweetDTO tweetDTO = new TweetDTO();
            tweetDTO.setMessage("msg");
            tweetDTO.setPaths(singletonList("/user/" + login + "/drive/image.jpg"));
            when(twitterService.publishTweet(eq(login), any(TweetDTO.class)))
                    .thenAnswer(invocationOnMock -> {
                        TweetDTO argument = (TweetDTO) invocationOnMock.getArguments()[1];
                        argument.setUrl("/tweet/url");

                        return argument;
                    });

            MockHttpServletRequestBuilder post = post("/api/{login}/tweet", login)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(tweetDTO));

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("url", is("/tweet/url")))
                    .andExpect(jsonPath("responseMessage", is("twitter.tweet.publish.success")));
            verify(twitterService, times(1)).publishTweet(any(), any());
        }

        @Test
        void forAnonymous() throws Exception {
            //Given
            TweetDTO tweetDTO = new TweetDTO();
            tweetDTO.setMessage("Tweet message)");
            tweetDTO.setPaths(singletonList("/user/" + login + "/drive/image.jpg"));
            MockHttpServletRequestBuilder post = post("/api/{login}/tweet", login)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(tweetDTO));

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("http*/**/login"));
            verifyZeroInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void twitterServiceThrowsNotFoundTwitterAssociatedAccountException() throws Exception {
            //Given
            TweetDTO tweetDTO = new TweetDTO();
            tweetDTO.setMessage("Tweet message)");
            tweetDTO.setPaths(singletonList("/user/" + login + "/drive/image.jpg"));
            when(twitterService.publishTweet(any(String.class), any(TweetDTO.class)))
                    .thenThrow(NotFoundTwitterAssociatedAccountException.class);

            MockHttpServletRequestBuilder post = post("/api/{login}/tweet", login)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(tweetDTO));

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isForbidden())
                    .andExpect(content().string("twitter.error.notFoundAccount"));
            verify(twitterService, times(1)).publishTweet(eq(login), any(TweetDTO.class));
            verifyNoMoreInteractions(twitterService);
        }

        @Test
        @WithMockUser(username = "awesomeUser")
        void twitterServiceThrowsTweetPublishingException() throws Exception {
            //Given
            TweetDTO tweetDTO = new TweetDTO();
            tweetDTO.setMessage("Tweet message)");
            tweetDTO.setPaths(singletonList("/user/" + login + "/drive/image.jpg"));
            when(twitterService.publishTweet(any(String.class), any(TweetDTO.class)))
                    .thenThrow(TweetPublishingException.class);

            MockHttpServletRequestBuilder post = post("/api/{login}/tweet", login)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(tweetDTO));

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isBadRequest())
                    .andExpect(content().string("twitter.error.publishing"));
            verify(twitterService, times(1)).publishTweet(eq(login), any(TweetDTO.class));
            verifyNoMoreInteractions(twitterService);
        }
    }
}