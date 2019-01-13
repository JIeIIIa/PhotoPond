package ua.kiev.prog.photopond;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


public class TestControllerBasicConfiguration {
    protected MockMvc mockMvc;

    private static final String SERVER_ADDRESS = "https://localhost";

    protected void configure(Object... controllers) {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("classpath:static/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(controllers)
                .setViewResolvers(viewResolver)
                .build();
    }

    protected void matchViewNameAfterGetRequest(String url, String viewName) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SERVER_ADDRESS + url))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(viewName));
    }

}
