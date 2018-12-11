package ua.kiev.prog.photopond.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ua.kiev.prog.photopond.configuration.UserInfoServiceMockConfiguration;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;
import static ua.kiev.prog.photopond.user.UserRole.ADMIN;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserAdministrationController.class, secure = false)
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        UserInfoServiceMockConfiguration.class
})
@ActiveProfiles({DISK_DATABASE_STORAGE, "unitTest"})
public class UserAdministrationControllerTest {
    private static final String URL_PREFIX = "/administration/user";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserInfoService userInfoService;

    @Test
    public void getAllUsers() throws Exception {
        MockHttpServletRequestBuilder get = get("/administration/users");
        when(userInfoService.findAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get)
                .andExpect(status().isOk());
    }

    @Test
    public void updateExistsUserSameIdInPathAndModelVariables() throws Exception {
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(10L)
                .login("someUser")
                .password("password")
                .build();

        updateUserSuccess(10, userInfoDTO);
    }


    @Test
    public void updateExistsUserDifferentIdInPathAndModelVariables() throws Exception {
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(22L)
                .login("someUser")
                .password("password")
                .build();

        updateUserSuccess(77, userInfoDTO);
    }

    private void updateUserSuccess(int pathId, UserInfoDTO userDTO) throws Exception {
        when(userInfoService.updateBaseInformation(any()))
                .thenAnswer(
                        (Answer<Optional<UserInfoDTO>>) invocationOnMock -> Optional.ofNullable((UserInfoDTO) invocationOnMock.getArguments()[0])
                );
        String expectedJsonContent = "{\"id\":" + pathId + ",\"login\":\"someUser\",\"role\":\"USER\"}";

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(URL_PREFIX + "/" + pathId)
                .content(buildJsonContent(userDTO))
                .contentType(MediaType.APPLICATION_JSON);
        if (userDTO.getId() != pathId) {
            post.param("id", String.valueOf(userDTO.getId()));
        }

        mockMvc.perform(post)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.content().json(expectedJsonContent, true))
                .andDo(print());
    }

    @Test
    public void updateNotExistsUser() throws Exception {
        when(userInfoService.updateBaseInformation(any(UserInfoDTO.class)))
                .thenReturn(Optional.empty());
        UserInfoDTO user = UserInfoDTOBuilder.getInstance()
                .id(77L)
                .login("user")
                .password("password")
                .role(ADMIN)
                .build();
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(URL_PREFIX + "/77")
                .content(buildJsonContent(user))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(post)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteExistsUser() throws Exception {
        UserInfoDTO userDTO = UserInfoDTOBuilder.getInstance()
                .id(77L)
                .login("someUser")
                .password("password")
                .build();
        when(userInfoService.delete(userDTO.getId()))
                .thenReturn(Optional.of(userDTO));
        String jsonContent = "{\"id\":77,\"login\":\"someUser\",\"role\":\"USER\"}";

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders.delete(URL_PREFIX + "/77");

        mockMvc.perform(delete)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(jsonContent))
                .andDo(print());
    }

    @Test
    public void deleteNotExistsUser() throws Exception {
        when(userInfoService.updateBaseInformation(any(UserInfoDTO.class)))
                .thenReturn(null);

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders.delete(URL_PREFIX + "/77");

        mockMvc.perform(delete)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(print());
    }

    private String buildJsonContent(UserInfoDTO userDTO) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(userDTO);
    }
}