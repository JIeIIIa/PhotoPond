package ua.kiev.prog.photopond.twitter;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TwitterConstantsConfiguration {
    @Bean
    TwitterConstants twitterConstants() {
        TwitterConstants twitterConstants = new TwitterConstants();
        twitterConstants.setCallbackHost("https://localhost");
        twitterConstants.setConsumerKey("1234567");
        twitterConstants.setConsumerSecret("secret");
        TwitterConstants.postConstructUpdateCallbackUrl();

        return twitterConstants;
    }

}
