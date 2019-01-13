package ua.kiev.prog.photopond.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TwitterConstants {
    static final String ACCOUNT_VIEW_URL = "/private/twitter/account/view";

    private static String consumerKey;
    private static String consumerSecret;

    private static String callbackHost;
    private static String associateCallbackUrl;
    private static String loginCallbackUrl;

    @Value("${socials.callback.host}")
    public void setCallbackHost(String callbackHost) {
        TwitterConstants.callbackHost = callbackHost;
    }

    @PostConstruct
    static void postConstructUpdateCallbackUrl() {
        TwitterConstants.associateCallbackUrl = callbackHost + TwitterRequestMappingConstants.ASSOCIATE_CALLBACK_SHORT_URL;
        TwitterConstants.loginCallbackUrl = callbackHost + TwitterRequestMappingConstants.LOGIN_CALLBACK_SHORT_URL;
    }

    static String getAssociateCallbackUrl() {
        return TwitterConstants.associateCallbackUrl;
    }

    public static String getLoginCallbackUrl() {
        return TwitterConstants.loginCallbackUrl;
    }
    
    public static String getConsumerKey() {
        return consumerKey;
    }

    @Value("${twitter.consumer.key}")
    public void setConsumerKey(String consumerKey) {
        TwitterConstants.consumerKey = consumerKey;
    }

    public static String getConsumerSecret() {
        return consumerSecret;
    }

    @Value("${twitter.consumer.secret}")
    public void setConsumerSecret(String consumerSecret) {
        TwitterConstants.consumerSecret = consumerSecret;
    }
}
