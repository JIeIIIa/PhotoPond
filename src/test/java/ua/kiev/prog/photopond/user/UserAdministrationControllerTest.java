package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserAdministrationController.class)
@TestPropertySource(properties = "spring.profiles.active = SECURITY_MOCK")
public class UserAdministrationControllerTest {
    private static final String URL_PREFIX = "/administration/user";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInfoService userInfoService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllUsers() throws Exception {
        UserInfo userOne = new UserInfo("One", "qwerty", UserRole.USER);
        UserInfo userTwo = new UserInfo("Two", "asdfgh", UserRole.ADMIN);
        List<UserInfo> usersList = Arrays.asList(userOne, userTwo);
        Mockito.when(userInfoService.getAllUsers()).thenReturn(usersList);

        MockHttpServletRequestBuilder get = get(URL_PREFIX);

        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(model().attribute("usersList", usersList))
                .andExpect(MockMvcResultMatchers.view().name("/users/allUsers"));
        Mockito.verify(userInfoService, times(1)).getAllUsers();
    }

    @Test
    public void updateExistsUserSameIdInPathAndModelVariables() throws Exception {
        UserInfo user = new UserInfo("someUser", "password");
        user.setId(10);
        updateUserSuccess(10, user);
    }


    @Test
    public void updateExistsUserDifferentIdInPathAndModelVariables() throws Exception {
        UserInfo user = new UserInfo("someUser", "password");
        user.setId(22);
        updateUserSuccess(77, user);
    }

    private void updateUserSuccess(int pathId, UserInfo user) throws Exception {
        Mockito.when(userInfoService.update(Matchers.<UserInfo>any()))
                .thenAnswer(new Answer<UserInfo>() {
                    @Override
                    public UserInfo answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return (UserInfo) invocationOnMock.getArguments()[0];
                    }
                });
        String jsonContent = "{\"id\":" + pathId + ",\"login\":\"someUser\",\"password\":\"password\",\"role\":\"USER\"}";

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(URL_PREFIX + "/" + pathId)
                .param("login", user.getLogin())
                .param("password", user.getPassword())
                .param("role", user.getRole().name())
                .contentType(MediaType.MULTIPART_FORM_DATA);
        if (user.getId() != pathId) {
            post.param("id", String.valueOf(user.getId()));
        }

        mockMvc.perform(post)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.content().json(jsonContent))
                .andDo(print());
    }

    @Test
    public void updateNotExistsUser() throws Exception {
        when(userInfoService.update(any(UserInfo.class)))
                .thenReturn(null);
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(URL_PREFIX + "/77")
                .param("login", "user")
                .param("password", "password")
                .param("role", "USER");

        mockMvc.perform(post)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteExistsUser() throws Exception {
        UserInfo user = new UserInfo("someUser", "password");
        user.setId(77);
        when(userInfoService.delete(user.getId()))
                .thenReturn(user);
        String jsonContent = "{\"id\":77,\"login\":\"someUser\",\"password\":\"password\",\"role\":\"USER\"}";

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders.delete(URL_PREFIX + "/77");

        mockMvc.perform(delete)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(jsonContent))
                .andDo(print());
    }

    @Test
    public void deleteNotExistsUser() throws Exception {
        when(userInfoService.update(any(UserInfo.class)))
                .thenReturn(null);

        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders.delete(URL_PREFIX + "/77");

        mockMvc.perform(delete)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(print());
    }
}