package ua.kiev.prog.photopond.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ua.kiev.prog.photopond.TestControllerBasicConfiguration;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
public class WelcomeControllerTest extends TestControllerBasicConfiguration {

    private static final String ROOT_VIEW_NAME = "index";

    @BeforeEach
    public void setup() {
        configure(new WelcomeController());
    }

    @Test
    public void rootUrlTest() throws Exception {
        matchViewNameAfterGetRequest("/", ROOT_VIEW_NAME);
    }

    @Test
    public void indexWithoutSuffixUrlTest() throws Exception {
        matchViewNameAfterGetRequest("/index", ROOT_VIEW_NAME);
    }

    @Test
    public void indexWithSuffixTest() throws Exception {
        matchViewNameAfterGetRequest("/index.html", ROOT_VIEW_NAME);
    }

    @Test
    public void indexWithBadSuffixUrlTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("index.hhttmmll"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }


}
