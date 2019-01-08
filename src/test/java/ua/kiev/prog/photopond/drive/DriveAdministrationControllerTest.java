package ua.kiev.prog.photopond.drive;

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
import ua.kiev.prog.photopond.drive.exception.DriveControllerAdvice;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {DriveAdministrationController.class, DriveControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class DriveAdministrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriveService driveService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void fullStatistics() throws Exception {
        //Given
        DriveStatisticsDTO first = new DriveStatisticsDTO("User", 123456L);
        first.setPictureCount(10L);
        DriveStatisticsDTO second = new DriveStatisticsDTO("Admin", 234567L);
        second.setPictureCount(20L);
        when(driveService.fullStatistics())
                .thenReturn(asList(first, second));
        MockHttpServletRequestBuilder get = get("/administration/drive/statistics");

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].login", is("User")))
                .andExpect(jsonPath("$[0].pictureCount", is(10)))
                .andExpect(jsonPath("$[0].sizeInKiloBytes", is("120.56")))
                .andExpect(jsonPath("$[1].login", is("Admin")))
                .andExpect(jsonPath("$[1].pictureCount", is(20)))
                .andExpect(jsonPath("$[1].sizeInKiloBytes", is("229.07")));

        verify(driveService, times(1)).fullStatistics();
        verifyNoMoreInteractions(driveService);
    }
}