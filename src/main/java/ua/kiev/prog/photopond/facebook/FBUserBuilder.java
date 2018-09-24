package ua.kiev.prog.photopond.facebook;

import ua.kiev.prog.photopond.user.UserInfo;

import java.time.LocalDateTime;

public class FBUserBuilder {
    private Long id;

    private String fbId;

    private String email;

    private String name;

    private String accessToken;

    private LocalDateTime tokenExpires;

    private UserInfo userInfo;

    private FBUserBuilder() {
        id = Long.MIN_VALUE;
    }

    public static FBUserBuilder getInstance() {
        return new FBUserBuilder();
    }

    public FBUserBuilder id(Long id) {
        this.id = id;

        return this;
    }

    public FBUserBuilder fbId(String fbId) {
        this.fbId = fbId;

        return this;
    }

    public FBUserBuilder email(String email) {
        this.email = email;

        return this;
    }

    public FBUserBuilder name(String firstName) {
        this.name = firstName;

        return this;
    }

    public FBUserBuilder accessToken(String accessToken) {
        this.accessToken = accessToken;

        return this;
    }

    public FBUserBuilder tokenExpires(LocalDateTime tokenExpires) {
        this.tokenExpires = tokenExpires;

        return this;
    }

    public FBUserBuilder userInfo(UserInfo userInfo) {
        this.userInfo = userInfo;

        return this;
    }

    public FBUser build() {
        FBUser fbUser = new FBUser();
        fbUser.setId(id);
        fbUser.setFbId(fbId);
        fbUser.setEmail(email);
        fbUser.setName(name);
        fbUser.setAccessToken(accessToken);
        fbUser.setTokenExpires(tokenExpires);
        fbUser.setUserInfo(userInfo);

        return fbUser;
    }
}
