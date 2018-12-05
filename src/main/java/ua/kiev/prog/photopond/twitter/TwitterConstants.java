package ua.kiev.prog.photopond.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TwitterConstants {
    static final String ACCOUNT_VIEW_URL = "/private/twitter/account/view";

    private static String consumerKey;
    private static String consumerSecret;


    private static String server;

    private static int port;

    private static String associateCallbackUrl;
    private static String loginCallbackUrl;




    @Value("${server.address:''}")
    public void setServer(String server) {
        TwitterConstants.server = server;
    }

    @Value("${server.port}")
    public void setPort(int port) {
        TwitterConstants.port = port;
    }

    @PostConstruct
    private static void updateCallbackUrl() {
        TwitterConstants.associateCallbackUrl = String.format("https://%s:%d%s", server, port,
                TwitterRequestMappingConstants.ASSOCIATE_CALLBACK_SHORT_URL);
        TwitterConstants.loginCallbackUrl = String.format("https://%s:%d%s", server, port,
                TwitterRequestMappingConstants.LOGIN_CALLBACK_SHORT_URL);
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
