package ua.kiev.prog.photopond.twitter;

public interface TwitterRequestMappingConstants {
    String ACCOUNT_VIEW_URL = "/private/twitter/account/view";
    String ASSOCIATE_ACCOUNT_URL = "/private/twitter/account/associate";
    String DISASSOCIATE_ACCOUNT_URL = "/private/twitter/account/remove";
    String AUTHENTICATION_WITH_TWITTER_URL = "/public/twitter/authentication";

    String CALLBACK_SHORT_URL = "/public/twitter-auth";
    String ASSOCIATE_CALLBACK_SHORT_URL = CALLBACK_SHORT_URL + "/callback/associate";
    String LOGIN_CALLBACK_SHORT_URL = CALLBACK_SHORT_URL + "/callback/login";
}
