
package ua.kiev.prog.photopond.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ua.kiev.prog.photopond.annotation.IntegrationTest;

import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DEV;
import static ua.kiev.prog.photopond.annotation.profile.ProfileConstants.DISK_DATABASE_STORAGE;

@ExtendWith(SpringExtension.class)
@IntegrationTest
@ActiveProfiles({DEV, DISK_DATABASE_STORAGE, "test"})
class WelcomeControllerIT {
    @Autowired
    MockMvc mockMvc;

    private static final String SERVER_ADDRESS = "https://localhost";

    @Test
    void mappedRoot() throws Exception {
        checkIndex("/");
    }

    @Test
    void mappedIndex() throws Exception {
        checkIndex("/index");
    }

    @Test
    void mappedIndexHtml() throws Exception {
        checkIndex("/index.html");
    }

    @Test
    void about() throws Exception {
        requestAndMatch("/about", "about", "<title>PhotoPond - About</title>");
        mockMvc.perform(get(SERVER_ADDRESS + "/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    void privacyPolicy() throws Exception {
        requestAndMatch("/public/privacyPolicy", "privacyPolicy", "<title>PhotoPond - Privacy policy</title>");
    }


    @Test
    void terms() throws Exception {
        requestAndMatch("/public/terms", "terms", "<title>PhotoPond - Terms of Service</title>");
    }

    private void checkIndex(String url) throws Exception {
        requestAndMatch(url, "index", "<title>PhotoPond - Welcome</title>");
    }

    private void requestAndMatch(String url, String viewName, String content) throws Exception {
        LocaleContextHolder.setLocale(new Locale("en", "EN"));
        mockMvc.perform(MockMvcRequestBuilders.get(SERVER_ADDRESS + url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(viewName))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString(content)));
    }
}
