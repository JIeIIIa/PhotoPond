package ua.kiev.prog.photopond.twitter;

import ua.kiev.prog.photopond.user.UserInfo;

public class TwitterUserBuilder {
    private Long id;

    private Long socialId;

    private String token;

    private String tokenSecret;

    private String name;

    private UserInfo userInfo;

    private TwitterUserBuilder() {
    }

    public static TwitterUserBuilder getInstance() {
        return new TwitterUserBuilder();
    }

    public TwitterUserBuilder id(Long id) {
        this.id = id;

        return this;
    }

    public TwitterUserBuilder socialId(Long socialId) {
        this.socialId = socialId;

        return this;
    }

    public TwitterUserBuilder token(String token) {
        this.token = token;

        return this;
    }

    public TwitterUserBuilder tokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;

        return this;
    }

    public TwitterUserBuilder name(String name) {
        this.name = name;

        return this;
    }

    public TwitterUserBuilder userInfo(UserInfo userInfo) {
        this.userInfo = userInfo;

        return this;
    }

    public TwitterUser build() {
        TwitterUser twitterUser = new TwitterUser();

        twitterUser.setId(id);
        twitterUser.setSocialId(socialId);
        twitterUser.setName(name);
        twitterUser.setToken(token);
        twitterUser.setTokenSecret(tokenSecret);
        twitterUser.setUserInfo(userInfo);

        return twitterUser;
    }
}
