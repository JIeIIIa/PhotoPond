package ua.kiev.prog.photopond.drive;

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
import ua.kiev.prog.photopond.Utils.TestUtils;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.drive.exception.DirectoryException;
import ua.kiev.prog.photopond.drive.exception.DriveControllerAdvice;
import ua.kiev.prog.photopond.drive.exception.DriveException;
import ua.kiev.prog.photopond.security.SpringSecurityWebAuthenticationTestConfiguration;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.drive.DriveItemType.DIR;
import static ua.kiev.prog.photopond.drive.DriveItemType.FILE;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {DriveController.class, DriveControllerAdvice.class})
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
        SpringSecurityWebAuthenticationTestConfiguration.class
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test", "securityWebAuthTestConfig"})
class DriveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriveService driveService;

    private static final String LOGIN = "awesomeUser";

    @BeforeEach
    void setUp() {
        reset(driveService);
    }

    @Test
    @WithMockUser(username = LOGIN)
    void directoryPage() throws Exception {
        //Given
        MockHttpServletRequestBuilder get = get("/user/{login}/drive/someDirectory", LOGIN);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform.andExpect(status().isOk())
                .andExpect(view().name("drive/directory"));
        verifyNoMoreInteractions(driveService);
    }

    @Nested
    @WithMockUser(username = LOGIN)
    class MoveDirectory {
        String path;
        String newName;
        String newPath;
        DriveItemDTO driveItemDTO;

        @BeforeEach
        void setUp() {
            path = "/directory";
            newName = "newDirectoryName";
            newPath = "/some/new/path";
            driveItemDTO = DriveItemDTOBuilder.getInstance()
                    .name(newName)
                    .parentUri(buildPath("user", LOGIN, "files", newPath))
                    .type(DIR)
                    .build();
        }

        @Test
        void success() throws Exception {
            //Given
            MockHttpServletRequestBuilder put = put("/user/{login}/drive{path}", LOGIN, path)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(TestUtils.convertObjectToJsonBytes(driveItemDTO));


            //When
            ResultActions perform = mockMvc.perform(put);

            //Then
            perform.andExpect(status().isOk());
            verify(driveService, times(1)).moveDirectory(LOGIN, path, buildPath(newPath, newName));
            verifyNoMoreInteractions(driveService);
        }

        @Test
        void wrongType() throws Exception {
            //Given
            driveItemDTO.setType(FILE);
            MockHttpServletRequestBuilder put = put("/user/{login}/drive{path}", LOGIN, path)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(TestUtils.convertObjectToJsonBytes(driveItemDTO));

            //When
            ResultActions perform = mockMvc.perform(put);

            //Then
            perform.andExpect(status().isBadRequest());
            verifyNoMoreInteractions(driveService);
        }

        @Test
        void failure() throws Exception {
            //Given
            doThrow(DriveException.class).when(driveService).moveDirectory(any(String.class), any(String.class), any(String.class));
            MockHttpServletRequestBuilder put = put("/user/{login}/drive{path}", LOGIN, path)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(TestUtils.convertObjectToJsonBytes(driveItemDTO));

            //When
            ResultActions perform = mockMvc.perform(put);

            //Then
            perform.andExpect(status().isNoContent());
            verify(driveService, times(1)).moveDirectory(LOGIN, path, buildPath(newPath, newName));
            verifyNoMoreInteractions(driveService);
        }
    }

    @Nested
    @WithMockUser(username = LOGIN)
    class DeleteDirectory {
        String path;

        @BeforeEach
        void setUp() {
            path = "/directory/path";
        }

        @Test
        void success() throws Exception {
            //Given
            MockHttpServletRequestBuilder delete = delete("/user/{login}/drive{path}", LOGIN, path);

            //When
            ResultActions perform = mockMvc.perform(delete);

            //Then
            perform.andExpect(status().isOk());
            verify(driveService, times(1)).deleteDirectory(LOGIN, path);
            verifyNoMoreInteractions(driveService);
        }

        @Test
        void failure() throws Exception {
            //Given
            doThrow(DirectoryException.class).when(driveService).deleteDirectory(any(), any());
            MockHttpServletRequestBuilder delete = delete("/user/{login}/drive{path}", LOGIN, path);

            //When
            ResultActions perform = mockMvc.perform(delete);

            //Then
            perform.andExpect(status().isNoContent());
            verify(driveService, times(1)).deleteDirectory(LOGIN, path);
            verifyNoMoreInteractions(driveService);
        }
    }
}