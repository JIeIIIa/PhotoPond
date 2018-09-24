package ua.kiev.prog.photopond.facebook;

import com.restfb.FacebookClient;
import com.restfb.types.User;
import ua.kiev.prog.photopond.user.UserInfo;

import java.time.ZoneId;
import java.util.Objects;

public class FBUserMapper {
    public static FBUserDTO toDto(FBUser fbUser) {
        return FBUserDTOBuilder.getInstance()
                .email(fbUser.getEmail())
                .fbId(fbUser.getFbId())
                .name(fbUser.getName())
                .build();
    }

    public static FBUser toEntity(User user, FacebookClient.AccessToken accessToken, UserInfo userInfo) {
        FBUserBuilder fbUserBuilder = FBUserBuilder.getInstance()
                .fbId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .userInfo(userInfo);
        if (Objects.nonNull(accessToken)) {
            fbUserBuilder
                    .accessToken(accessToken.getAccessToken())
                    .tokenExpires(accessToken.getExpires().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (Objects.nonNull(userInfo)) {
            fbUserBuilder
                    .userInfo(userInfo);
        }
        return fbUserBuilder.build();
    }
}
