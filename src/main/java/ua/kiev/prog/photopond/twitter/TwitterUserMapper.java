package ua.kiev.prog.photopond.twitter;

import twitter4j.User;
import twitter4j.auth.AccessToken;
import ua.kiev.prog.photopond.user.UserInfo;

public class TwitterUserMapper {
    public static TwitterUser toEntity(User user, AccessToken accessToken, UserInfo userInfo) {
        return TwitterUserBuilder.getInstance()
                .socialId(user.getId())
                .name(user.getScreenName())
                .token(accessToken.getToken())
                .tokenSecret(accessToken.getTokenSecret())
                .userInfo(userInfo)
                .build();
    }

    public static TwitterUserDTO toDto(TwitterUser twitterUser) {
        return TwitterUserDTOBuilder.getInstance()
                .id(twitterUser.getId())
                .socialId(twitterUser.getSocialId())
                .name(twitterUser.getName())
                .build();
    }
}
