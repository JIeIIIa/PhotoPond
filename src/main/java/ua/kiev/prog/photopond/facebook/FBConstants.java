package ua.kiev.prog.photopond.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FBConstants {

    static final Version FB_CLIENT_VERSION = Version.VERSION_3_1;

    static final String FB_CALLBACK_URL = "/public/fb-auth";

    private static String APPLICATION_ID;
    private static String APPLICATION_SECRET;

    private static final FacebookClient CLIENT = new DefaultFacebookClient(FB_CLIENT_VERSION);

    private static final ScopeBuilder SCOPE_BUILDER = new ScopeBuilder();
    static {
        SCOPE_BUILDER.addPermission(FacebookPermissions.EMAIL);
        SCOPE_BUILDER.addPermission(FacebookPermissions.PUBLIC_PROFILE);
    }

    private static String server;

    private static int port;

    private static String fullCallbackUrl;
    private static String associateAccountUrl;
    private static String loginUrl;


    @Value("${server.address}")
    public void setServer(String server) {
        FBConstants.server = server;
    }

    @Value("${server.port}")
    public void setPort(int port) {
        FBConstants.port = port;
    }

    @Value("${facebook.application.id}")
    public void setApplicationId(String applicationId) {
        FBConstants.APPLICATION_ID = applicationId;
    }

    @Value("${facebook.application.secret}")
    public void setApplicationSecret(String applicationSecret) {
        FBConstants.APPLICATION_SECRET = applicationSecret;
    }

    @PostConstruct
    private static void updateFullCallbackUrl() {
        FBConstants.fullCallbackUrl = String.format("https://%s:%d%s", server, port, FB_CALLBACK_URL);
        FBConstants.associateAccountUrl = getOAuthUrl(FBState.ASSOCIATE);
        FBConstants.loginUrl = getOAuthUrl(FBState.LOGIN);
    }

    static String getApplicationId(){
        return FBConstants.APPLICATION_ID;
    }

    static String getApplicationSecret(){
        return FBConstants.APPLICATION_SECRET;
    }

    static String getFullCallbackUrl() {
        return FBConstants.fullCallbackUrl;
    }

    static String associateAccountUrl() {
        return FBConstants.associateAccountUrl;
    }

    static String facebookLoginUrl() {
        return FBConstants.loginUrl;
    }

    private static String getOAuthUrl(FBState state) {
        return CLIENT.getLoginDialogUrl(getApplicationId(), getFullCallbackUrl(), SCOPE_BUILDER,
                Parameter.with("state", state),
                Parameter.with("auth_type", "rerequest"));
    }
}
