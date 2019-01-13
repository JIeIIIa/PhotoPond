package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.Utils.TestUtils.convertObjectToJsonBytes;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserSettingsController.class)
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class UserSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInfoService userInfoService;

    @MockBean
    private BindingErrorResolver bindingErrorResolver;

    private static final String LOGIN = "UserLogin";
    private static final String URL = "/user/" + LOGIN + "/settings";
    private static final ArgumentMatcher<UserInfoDTO> USER_INFO_DTO_ARGUMENT_MATCHER = userInfoDTO -> LOGIN.equals(userInfoDTO.getLogin());

    @BeforeEach
    void setUp() {
        reset(userInfoService);
        reset(bindingErrorResolver);
    }

    @Test
    @WithMockUser(username = LOGIN)
    void allSettings() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(URL);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform
                .andExpect(status().isOk())
                .andExpect(view().name("settings/settings"));
        verifyNoMoreInteractions(userInfoService);
        verifyNoMoreInteractions(bindingErrorResolver);
    }


    static Stream<UserInfoDTO> userInfoDTOPassword() {
        return Stream.of(
                UserInfoDTOBuilder.getInstance()
                        .oldPassword("oldPassword").password("newPassword").passwordConfirmation("newPassword")
                        .build(),
                UserInfoDTOBuilder.getInstance()
                        .login(LOGIN).password("newPassword").passwordConfirmation("newPassword")
                        .build(),
                UserInfoDTOBuilder.getInstance()
                        .login(LOGIN).oldPassword("password").passwordConfirmation("newPassword")
                        .build(),
                UserInfoDTOBuilder.getInstance()
                        .login(LOGIN).oldPassword("password").password("newPassword")
                        .build(),
                UserInfoDTOBuilder.getInstance()
                        .login(LOGIN).oldPassword("password").password("newPassword").passwordConfirmation("newPasswordWrong")
                        .build()
        );
    }

    @Nested
    @WithMockUser(username = LOGIN)
    class ChangePassword {
        MockHttpServletRequestBuilder put;
        UserInfoDTO userInfoDTO;

        @BeforeEach
        void setUp() {
            put = put(URL + "/password");
            userInfoDTO = UserInfoDTOBuilder.getInstance()
                    .login(LOGIN)
                    .oldPassword("password")
                    .password("newPassword")
                    .passwordConfirmation("newPassword")
                    .build();
        }

        void successTestExecutor() throws Exception {
            //Given
            when(userInfoService.setNewPassword(any())).thenReturn(true);
            String key = "user.password.change.success";
            String value = "PasswordChanged";
            when(bindingErrorResolver.resolveMessage(eq(key), any())).thenReturn(value);

            put
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(userInfoDTO));

            //When
            ResultActions perform = mockMvc.perform(put);

            //Then
            perform
                    .andExpect(status().isOk())
                    .andExpect(content().string(value));
            verify(userInfoService, times(1)).setNewPassword(argThat(USER_INFO_DTO_ARGUMENT_MATCHER));
            verify(bindingErrorResolver, times(1)).resolveMessage(eq(key), any());
            verifyNoMoreInteractions(userInfoService);
            verifyNoMoreInteractions(bindingErrorResolver);
        }

        @Test
        void success() throws Exception {
            successTestExecutor();
        }

        @Test
        void wrongLoginInSentUserInfoDTO() throws Exception {
            userInfoDTO.setLogin("anotherLogin");
            successTestExecutor();
        }

        @Test
        void failure() throws Exception {
            //Given
            when(userInfoService.setNewPassword(any())).thenReturn(false);
            String key = "user.password.change.error";
            String value = "PasswordNotChanged";
            when(bindingErrorResolver.resolveMessage(eq(key), any())).thenReturn(value);

            put
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(userInfoDTO));

            //When
            ResultActions perform = mockMvc.perform(put);

            //Then
            perform
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().string(value));
            verify(userInfoService, times(1)).setNewPassword(argThat(USER_INFO_DTO_ARGUMENT_MATCHER));
            verify(bindingErrorResolver, times(1)).resolveMessage(eq(key), any());
            verifyNoMoreInteractions(userInfoService);
            verifyNoMoreInteractions(bindingErrorResolver);
        }

        @ParameterizedTest(name = "[{index}] ==> {0}")
        @MethodSource(value = {"ua.kiev.prog.photopond.user.UserSettingsControllerTest#userInfoDTOPassword()"})
        void wrongContent(UserInfoDTO userInfoDTO) throws Exception {
            //Given
            put
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(convertObjectToJsonBytes(userInfoDTO));

            //When
            ResultActions perform = mockMvc.perform(put);

            //Then
            perform.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @WithMockUser(username = LOGIN)
    class UpdateAvatar {
        MockHttpServletRequestBuilder post;
        UserInfoDTO userInfoDTO;
        MockMultipartFile mockMultipartFile;

        @BeforeEach
        void setUp() {
            String filename = "realName.jpg";
            mockMultipartFile = new MockMultipartFile("avatar", filename, "img/jpg", filename.getBytes());
            userInfoDTO = UserInfoDTOBuilder.getInstance()
                    .login(LOGIN)
                    .avatar(mockMultipartFile)
                    .build();
        }

        void successTestExecutor(String userLogin) throws Exception {
            //Given
            when(userInfoService.updateAvatar(any())).thenReturn(true);
            String key = "user.avatar.change.success";
            String value = "AvatarChanged";
            when(bindingErrorResolver.resolveMessage(eq(key), any())).thenReturn(value);

            post = multipart(URL + "/avatar")
                    .file(mockMultipartFile);
            if (nonNull(userLogin)) {
                post.param("login", userLogin);
            }

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform
                    .andExpect(status().isOk())
                    .andExpect(content().string(value));
            verify(userInfoService, times(1)).updateAvatar(argThat(USER_INFO_DTO_ARGUMENT_MATCHER));
            verify(bindingErrorResolver, times(1)).resolveMessage(eq(key), any());
            verifyNoMoreInteractions(userInfoService);
            verifyNoMoreInteractions(bindingErrorResolver);
        }

        @Test
        void success() throws Exception {
            successTestExecutor(LOGIN);
        }

        @Test
        void wrongLoginInSentUserInfoDTO() throws Exception {
            userInfoDTO.setLogin("anotherLogin");
            successTestExecutor(userInfoDTO.getLogin());
        }

        @Test
        void sendOnlyFile() throws Exception {
            successTestExecutor(null);
        }

        @Test
        void failure() throws Exception {
            //Given
            when(userInfoService.updateAvatar(any())).thenReturn(false);
            String key = "user.avatar.change.error";
            String value = "AvatarNotChanged";
            when(bindingErrorResolver.resolveMessage(eq(key), any())).thenReturn(value);

            post = multipart(URL + "/avatar")
                    .file(mockMultipartFile)
                    .param("login", LOGIN);


            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().string(value));
            verify(userInfoService, times(1)).updateAvatar(argThat(USER_INFO_DTO_ARGUMENT_MATCHER));
            verify(bindingErrorResolver, times(1)).resolveMessage(eq(key), any());
            verifyNoMoreInteractions(userInfoService);
            verifyNoMoreInteractions(bindingErrorResolver);
        }

        @Test
        void sendOnlyLogin() throws Exception {
            //Given
            post = multipart(URL + "/avatar")
                    .param("login", LOGIN);

            //When
            ResultActions perform = mockMvc.perform(post);

            //Then
            perform.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @WithMockUser(username = LOGIN)
    class DeleteAvatar {
        MockHttpServletRequestBuilder delete;

        @BeforeEach
        void setUp() {
            String filename = "realName.jpg";
        }

        void successTestExecutor(String userLogin) throws Exception {
            //Given
            when(userInfoService.updateAvatar(any())).thenReturn(true);
            String key = "user.avatar.remove.success";
            String value = "AvatarRemoved";
            when(bindingErrorResolver.resolveMessage(eq(key), any())).thenReturn(value);

            delete = delete(URL + "/avatar");
            if (nonNull(userLogin)) {
                delete.param("login", userLogin);
            }

            //When
            ResultActions perform = mockMvc.perform(delete);

            //Then
            perform
                    .andExpect(status().isOk())
                    .andExpect(content().string(value));
            verify(userInfoService, times(1)).updateAvatar(argThat(USER_INFO_DTO_ARGUMENT_MATCHER));
            verify(bindingErrorResolver, times(1)).resolveMessage(eq(key), any());
            verifyNoMoreInteractions(userInfoService);
            verifyNoMoreInteractions(bindingErrorResolver);
        }

        @Test
        void success() throws Exception {
            successTestExecutor(null);
        }

        @Test
        void wrongLoginInSentUserInfoDTO() throws Exception {
            successTestExecutor("wrongLogin");
        }

        @Test
        void failure() throws Exception {
            //Given
            when(userInfoService.updateAvatar(any())).thenReturn(false);
            String key = "user.avatar.change.error";
            String value = "AvatarNotChanged";
            when(bindingErrorResolver.resolveMessage(eq(key), any())).thenReturn(value);

            delete = delete(URL + "/avatar");

            //When
            ResultActions perform = mockMvc.perform(delete);

            //Then
            perform
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().string(value));
            verify(userInfoService, times(1)).updateAvatar(argThat(USER_INFO_DTO_ARGUMENT_MATCHER));
            verify(bindingErrorResolver, times(1)).resolveMessage(eq(key), any());
            verifyNoMoreInteractions(userInfoService);
            verifyNoMoreInteractions(bindingErrorResolver);
        }
    }

    @Nested
    @WithMockUser(username = "anotherUser")
    class ChangeOtherUserSettings {
        private final String ANOTHER_LOGIN= "anotherUser";
        void testExecutor(MockHttpServletRequestBuilder requestBuilders) throws Exception {
            //When
            ResultActions perform = mockMvc.perform(requestBuilders);

            //Then
            perform.andExpect(status().isUnauthorized());
        }

        @Test
        void changePassword() throws Exception {
            //Given
            UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                    .login("anotherUser")
                    .oldPassword("oldPassword")
                    .password("password")
                    .passwordConfirmation("password")
                    .build();
            MockHttpServletRequestBuilder put = put(URL + "/password")
                    .content(convertObjectToJsonBytes(userInfoDTO));

            testExecutor(put);
        }

        @Test
        void changeAvatar() throws Exception {
            //Given
            MockMultipartFile file = new MockMultipartFile("avatar", "file.jpg", "img/jpg", "file.jpg".getBytes());
            MockHttpServletRequestBuilder post = multipart(URL + "/avatar")
                    .file(file)
                    .param("login", ANOTHER_LOGIN);

            testExecutor(post);
        }

        @Test
        void deleteAvatar() throws Exception {
            //Given
            MockHttpServletRequestBuilder post = delete(URL + "/avatar").param("login", ANOTHER_LOGIN);

            testExecutor(post);
        }
    }
}