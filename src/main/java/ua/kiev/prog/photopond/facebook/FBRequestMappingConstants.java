package ua.kiev.prog.photopond.facebook;

public interface FBRequestMappingConstants {
    String ACCOUNT_VIEW_URL = "/private/facebook/account/view";
    String ACCOUNTS_LIST_URL = "/private/facebook/accounts";
    String ASSOCIATE_ACCOUNT_URL = "/private/facebook/account/associate";
    String DISASSOCIATE_ACCOUNT_URL = "/private/facebook/account/remove";
    String AUTHENTICATION_WITH_FACEBOOK_URL = "/public/facebook/authentication";

    String ERROR_ATTRIBUTE_NAME = "fbErrorMessage";
    String ERROR_AUTH_ATTRIBUTE_NAME = "fbAuthError";
}
