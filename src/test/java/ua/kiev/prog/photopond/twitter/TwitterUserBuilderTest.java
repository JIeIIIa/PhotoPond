package ua.kiev.prog.photopond.twitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class TwitterUserBuilderTest {

    @Test
    public void id() {
        //Given
        Long id = 777L;

        //When
        TwitterUser twitterUser = TwitterUserBuilder.getInstance().id(id).build();

        //Then
        assertThat(twitterUser.getId()).isEqualTo(id);
        assertThat(twitterUser).isEqualToIgnoringGivenFields(new TwitterUser(), "id");
    }

    @Test
    public void socialId() {
        //Given
        Long socialId = 777L;

        //When
        TwitterUser twitterUser = TwitterUserBuilder.getInstance().socialId(socialId).build();

        //Then
        assertThat(twitterUser.getSocialId()).isEqualTo(socialId);
        assertThat(twitterUser).isEqualToIgnoringGivenFields(new TwitterUser(), "socialId");
    }

    @Test
    public void token() {
        //Given
        String token = "awesomeToken";

        //When
        TwitterUser twitterUser = TwitterUserBuilder.getInstance().token(token).build();

        //Then
        assertThat(twitterUser.getToken()).isEqualTo(token);
        assertThat(twitterUser).isEqualToIgnoringGivenFields(new TwitterUser(), "token");
    }

    @Test
    public void tokenSecret() {
        //Given
        String tokenSecret = "awesomeTokenSecret";

        //When
        TwitterUser twitterUser = TwitterUserBuilder.getInstance().tokenSecret(tokenSecret).build();

        //Then
        assertThat(twitterUser.getTokenSecret()).isEqualTo(tokenSecret);
        assertThat(twitterUser).isEqualToIgnoringGivenFields(new TwitterUser(), "tokenSecret");
    }

    @Test
    public void name() {
        //Given
        String name = "Anonymous";

        //When
        TwitterUser twitterUser = TwitterUserBuilder.getInstance().name(name).build();

        //Then
        assertThat(twitterUser.getName()).isEqualTo(name);
        assertThat(twitterUser).isEqualToIgnoringGivenFields(new TwitterUser(), "name");
    }

    @Test
    public void userInfo() {
        //Given
        UserInfo userInfo = new UserInfoBuilder()
                .id(777L).login("login").password("password").role(UserRole.ADMIN)
                .build();
        UserInfo expectedUser = new UserInfo().copyFrom(userInfo);

        //When
        TwitterUser twitterUser = TwitterUserBuilder.getInstance().userInfo(userInfo).build();

        //Then
        assertThat(twitterUser.getUserInfo()).isEqualToComparingFieldByField(expectedUser);
        assertThat(twitterUser).isEqualToIgnoringGivenFields(new TwitterUser(), "userInfo");
    }
}
