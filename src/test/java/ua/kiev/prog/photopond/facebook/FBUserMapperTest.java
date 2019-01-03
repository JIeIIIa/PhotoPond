package ua.kiev.prog.photopond.facebook;

import com.restfb.FacebookClient;
import com.restfb.types.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ua.kiev.prog.photopond.user.UserInfo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ua.kiev.prog.photopond.facebook.FBUserMapper.toEntity;

class FBUserMapperTest {

    static Stream<FBUser> fbUsers() {
        return ThreadLocalRandom.current().longs(5, 1, 100000)
                .boxed().map(i ->
                        FBUserBuilder.getInstance()
                                .id(i)
                                .fbId("id" + i)
                                .name("name_" + i)
                                .accessToken("token_" + i)
                                .tokenExpires(LocalDateTime.now())
                                .email("box_" + i + "@email.com")
                                .build()
                );
    }

    @ParameterizedTest
    @MethodSource("fbUsers")
    void toDto(FBUser fbUser) {
        //Given
        String email = fbUser.getEmail();
        String fbId = fbUser.getFbId();
        String name = fbUser.getName();

        //When
        FBUserDTO fbUserDTO = FBUserMapper.toDto(fbUser);

        //Then
        assertThat(fbUserDTO.getEmail()).isEqualTo(email);
        assertThat(fbUserDTO.getFbId()).isEqualTo(fbId);
        assertThat(fbUserDTO.getName()).isEqualTo(name);

    }

    @ParameterizedTest
    @MethodSource("fbUsers")
    void toEntityUserNonNull(FBUser fbUser) {
        //Given
        User user = new User();
        user.setEmail(fbUser.getEmail());
        user.setId(fbUser.getFbId());
        user.setName(fbUser.getName());

        //When
        FBUser result = toEntity(user, null, null);

        //Then
        assertThat(result).isEqualToComparingOnlyGivenFields(fbUser, "email", "fbId", "name");
    }

    @Test
    void toEntityTokenNonNull() {
        //Given
        FacebookClient.AccessToken accessToken = mock(FacebookClient.AccessToken.class);
        when(accessToken.getAccessToken()).thenReturn("accessToken");
        when(accessToken.getExpires()).thenReturn(new Date(1234));

        //When
        FBUser result = toEntity(new User(), accessToken, null);

        //Then
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getTokenExpires()).isEqualTo(new Date(1234).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Test
    void toEntityUserInfoNonNull() {
        //Given
        UserInfo userInfo = new UserInfo("login", "password");

        //When
        FBUser result = toEntity(new User(), null, userInfo);

        //Then
        assertThat(result.getUserInfo()).isEqualTo(new UserInfo("login", "password"));
        assertThat(result).isEqualToIgnoringGivenFields(new FBUser(), "userInfo");
    }

    @Test
    void toEntityUserIsNull() {
        //When
        assertThrows(IllegalArgumentException.class, () -> toEntity(null, null, null));
    }
}