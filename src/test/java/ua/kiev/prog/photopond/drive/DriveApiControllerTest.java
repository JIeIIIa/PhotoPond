package ua.kiev.prog.photopond.drive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.configuration.WebMvcTestContextConfiguration;
import ua.kiev.prog.photopond.drive.exception.DriveException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DATABASE_STORAGE;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.drive.DriveItemType.DIR;
import static ua.kiev.prog.photopond.drive.DriveItemType.FILE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DriveApiController.class, secure = false)
@ContextConfiguration(classes = {
        WebMvcTestContextConfiguration.class,
})
@ActiveProfiles({DEV, DATABASE_STORAGE, "unitTest", "test"})
class DriveApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriveService driveService;

    private String dateAsString;
    private LocalDateTime localDateTime;
    private String login;
    private DriveItemDTO root;
    private DriveItemDTO someDirectory;
    private DriveItemDTO someDirectoryChild;
    private DriveItemDTO file;

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @BeforeEach
    void setUp() {
        login = "awesomeUser";
        localDateTime = LocalDateTime.now();
        Date date = toDate(localDateTime);
        root = DriveItemDTOBuilder.getInstance().name("/").parentUri("/").type(DIR).creationDate(date).build();
        someDirectory = DriveItemDTOBuilder.getInstance().name("someDirectory").parentUri("/").type(DIR).creationDate(date).build();
        someDirectoryChild = DriveItemDTOBuilder.getInstance().name("someDirectoryChild").parentUri("/someDirectory").type(DIR).creationDate(date).build();
        file = DriveItemDTOBuilder.getInstance().name("file.jpg").parentUri("/someDirectory").type(FILE).creationDate(date).build();
        dateAsString = root.getCreationDateString();
    }

    @Test
    void retrieveChildDirectories() throws Exception {
        //Given
        String path = "/someDirectory";
        DirectoriesDTO directoriesDTO = new DirectoriesDTO();
        directoriesDTO.setCurrent(someDirectory);
        directoriesDTO.setParent(root);
        directoriesDTO.setChildDirectories(singletonList(someDirectoryChild));

        MockHttpServletRequestBuilder get = get("/api/{login}/directories{path}", login, path);

        when(driveService.retrieveDirectories(login, path)).thenReturn(directoriesDTO);

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("parent.name", is("/")))
                .andExpect(jsonPath("parent.parentUri", is("/")))
                .andExpect(jsonPath("parent.type", is("DIR")))
                .andExpect(jsonPath("parent.creationDate", is(toDate(localDateTime).getTime())))
                .andExpect(jsonPath("parent.creationDateString", is(dateAsString)))
                .andExpect(jsonPath("current.name", is("someDirectory")))
                .andExpect(jsonPath("current.parentUri", is("/")))
                .andExpect(jsonPath("current.type", is("DIR")))
                .andExpect(jsonPath("current.creationDate", is(toDate(localDateTime).getTime())))
                .andExpect(jsonPath("current.creationDateString", is(dateAsString)))
                .andExpect(jsonPath("childDirectories", hasSize(1)))
                .andExpect(jsonPath("childDirectories[0].name", is("someDirectoryChild")))
                .andExpect(jsonPath("childDirectories[0].parentUri", is("/someDirectory")))
                .andExpect(jsonPath("childDirectories[0].type", is("DIR")))
                .andExpect(jsonPath("childDirectories[0].creationDate", is(toDate(localDateTime).getTime())))
                .andExpect(jsonPath("childDirectories[0].creationDateString", is(dateAsString)))
                .andReturn();
        verify(driveService, times(1)).retrieveDirectories(login, path);
        verifyNoMoreInteractions(driveService);
    }

    @Test
    void retrieveContent() throws Exception {
        //Given
        String path = "/someDirectory";
        MockHttpServletRequestBuilder get = get("/api/{login}/directory{path}", login, path);
        when(driveService.retrieveContent(login, path, true))
                .thenReturn(asList(someDirectoryChild, file));

        //When
        ResultActions perform = mockMvc.perform(get);

        //Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("someDirectoryChild")))
                .andExpect(jsonPath("$[0].parentUri", is("/someDirectory")))
                .andExpect(jsonPath("$[0].type", is("DIR")))
                .andExpect(jsonPath("$[1].name", is("file.jpg")))
                .andExpect(jsonPath("$[1].parentUri", is("/someDirectory")))
                .andExpect(jsonPath("$[1].type", is("FILE")))
                .andExpect(jsonPath("$[*].creationDate", everyItem(is(toDate(localDateTime).getTime()))))
                .andExpect(jsonPath("$[*].creationDateString", everyItem(is(dateAsString))))
                .andReturn();
        verify(driveService, times(1)).retrieveContent(login, path, true);
        verifyNoMoreInteractions(driveService);
    }

    @Test
    void createDirectory() throws Exception {
        //Given
        String path = "/someDirectory";
        String name = "someDirectoryChild";
        MockHttpServletRequestBuilder post = post("/api/{login}/directory{path}", login, path)
                .content(name);
        when(driveService.addDirectory(login, path, name))
                .thenReturn(someDirectoryChild);

        //When
        ResultActions perform = mockMvc.perform(post);

        //Then
        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("name", is("someDirectoryChild")))
                .andExpect(jsonPath("parentUri", is("/someDirectory")))
                .andExpect(jsonPath("type", is("DIR")))
                .andExpect(jsonPath("creationDate", is(toDate(localDateTime).getTime())))
                .andExpect(jsonPath("creationDateString", is(dateAsString)))
                .andReturn();
        verify(driveService, times(1)).addDirectory(login, path, name);
        verifyNoMoreInteractions(driveService);
    }

    @Test
    void addFiles() throws Exception {
        //Given
        String path = "/someDirectory";
        MockMultipartFile[] mockMultipartFiles = {
                new MockMultipartFile("files", "one.jpg", "image/png", "dataOne".getBytes()),
                new MockMultipartFile("files", "two.jpg", "image/png", "anotherData".getBytes())
        };

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/api/{login}/files{path}", login, path)
                .file(mockMultipartFiles[0])
                .file(mockMultipartFiles[1]);

        when(driveService.addPictureFile(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class), ArgumentMatchers.any(MultipartFile.class)))
                .thenAnswer(mockInvocation ->
                        DriveItemDTOBuilder.getInstance()
                                .name(((MultipartFile) mockInvocation.getArguments()[2]).getOriginalFilename())
                                .parentUri((String) mockInvocation.getArguments()[1])
                                .type(FILE)
                                .build());

        //When
        ResultActions perform = mockMvc.perform(requestBuilder);

        //Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].parentUri", everyItem(is(path))))
                .andExpect(jsonPath("$[0].name", is(mockMultipartFiles[0].getOriginalFilename())))
                .andExpect(jsonPath("$[1].name", is(mockMultipartFiles[1].getOriginalFilename())));
        verify(driveService, times(1)).addPictureFile(login, path, mockMultipartFiles[0]);
        verify(driveService, times(1)).addPictureFile(login, path, mockMultipartFiles[1]);
        verifyNoMoreInteractions(driveService);
    }

    @Test
    void addFilesWithSomeServiceError() throws Exception {
        //Given
        String path = "/someDirectory";
        MockMultipartFile[] mockMultipartFiles = {
                new MockMultipartFile("files", "one.jpg", "image/png", "dataOne".getBytes()),
                new MockMultipartFile("files", "two.jpg", "image/png", "anotherData".getBytes())
        };

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/api/{login}/files{path}", login, path)
                .file(mockMultipartFiles[0])
                .file(mockMultipartFiles[1]);

        when(driveService.addPictureFile(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class), eq(mockMultipartFiles[0])))
                .thenAnswer(mockInvocation ->
                        DriveItemDTOBuilder.getInstance()
                                .name(((MultipartFile) mockInvocation.getArguments()[2]).getOriginalFilename())
                                .parentUri((String) mockInvocation.getArguments()[1])
                                .type(FILE)
                                .build());
        when(driveService.addPictureFile(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class), eq(mockMultipartFiles[1])))
                .thenThrow(DriveException.class);

        //When
        ResultActions perform = mockMvc.perform(requestBuilder);

        //Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].parentUri", everyItem(is(path))))
                .andExpect(jsonPath("$[0].name", is(mockMultipartFiles[0].getOriginalFilename())));
        verify(driveService, times(1)).addPictureFile(login, path, mockMultipartFiles[0]);
        verify(driveService, times(1)).addPictureFile(login, path, mockMultipartFiles[1]);
        verifyNoMoreInteractions(driveService);
    }
}