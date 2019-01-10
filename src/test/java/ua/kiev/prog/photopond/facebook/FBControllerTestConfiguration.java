package ua.kiev.prog.photopond.facebook;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import ua.kiev.prog.photopond.core.BindingErrorResolver;

@TestConfiguration
class FBControllerTestConfiguration {
    @Bean
    FBConstants fbConstants() {
        FBConstants fbConstants = new FBConstants();
        fbConstants.setCallbackHost("https://localhost");
        fbConstants.setApplicationId("1234567");
        fbConstants.setApplicationSecret("secret");
        FBConstants.postConstructUpdateUrls();

        return fbConstants;
    }

    @MockBean
    BindingErrorResolver bindingErrorResolver;
}
