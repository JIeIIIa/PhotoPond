package ua.kiev.prog.photopond.facebook;

import org.junit.jupiter.api.Test;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class FBUserBuilderTest {

    @Test
    public void id() {
        //Given
        Long id = 777L;

        //When
        FBUser fbUser = FBUserBuilder.getInstance().id(id).build();

        //Then
        assertThat(fbUser.getId()).isEqualTo(id);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "id");
    }

    @Test
    public void fbId() {
        //Given
        String fbId = "someFacebookId";

        //When
        FBUser fbUser = FBUserBuilder.getInstance().fbId(fbId).build();

        //Then
        assertThat(fbUser.getFbId()).isEqualTo(fbId);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "fbId");
    }

    @Test
    public void email() {
        //Given
        String email = "box@email.com";

        //When
        FBUser fbUser = FBUserBuilder.getInstance().email(email).build();

        //Then
        assertThat(fbUser.getEmail()).isEqualTo(email);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "email");
    }

    @Test
    public void name() {
        //Given
        String name = "Anonymous";

        //When
        FBUser fbUser = FBUserBuilder.getInstance().name(name).build();

        //Then
        assertThat(fbUser.getName()).isEqualTo(name);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "name");
    }

    @Test
    public void accessToken() {
        //Given
        String accessToken = "awesomeAccessToken";

        //When
        FBUser fbUser = FBUserBuilder.getInstance().accessToken(accessToken).build();

        //Then
        assertThat(fbUser.getAccessToken()).isEqualTo(accessToken);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "accessToken");
    }

    @Test
    public void tokenExpires() {
        //Given
        LocalDateTime tokenExpires = LocalDateTime.now();

        //When
        FBUser fbUser = FBUserBuilder.getInstance().tokenExpires(tokenExpires).build();

        //Then
        assertThat(fbUser.getTokenExpires()).isEqualTo(tokenExpires);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "tokenExpires");
    }

    @Test
    public void userInfo() {
        //Given
        UserInfo userInfo = new UserInfoBuilder()
                .id(777L).login("login").password("password").role(UserRole.ADMIN)
                .build();
        UserInfo expectedUser = new UserInfo().copyFrom(userInfo);

        //When
        FBUser fbUser = FBUserBuilder.getInstance().userInfo(userInfo).build();

        //Then
        assertThat(fbUser.getUserInfo()).isEqualToComparingFieldByField(expectedUser);
        assertThat(fbUser).isEqualToIgnoringGivenFields(new FBUser(), "userInfo");
    }
}
