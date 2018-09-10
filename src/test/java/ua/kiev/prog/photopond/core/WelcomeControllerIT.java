package ua.kiev.prog.photopond.core;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ua.kiev.prog.photopond.annotation.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@IntegrationTest
@ActiveProfiles("dev")
public class WelcomeControllerIT {
    @Autowired
    MockMvc mockMvc;

    private static final String SERVER_ADDRESS = "https://localhost";

    @Test
    public void mappedRoot() throws Exception {
        checkIndex(SERVER_ADDRESS + "/");
    }

    @Test
    public void mappedIndex() throws Exception {
        checkIndex(SERVER_ADDRESS + "/index");
    }

    @Test
    public void mappedIndexHtml() throws Exception {
        checkIndex(SERVER_ADDRESS + "/index.html");
    }

    private void checkIndex(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("index"))
                .andExpect(content().string(Matchers.containsString("PhotoPond")))
                .andDo(print());
    }
}
