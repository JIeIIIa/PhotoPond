package ua.kiev.prog.photopond.twitter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoBuilder;
import ua.kiev.prog.photopond.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TwitterUserMapperTest {

    @Test
    void toEntity() {
        //Given
        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(12345L);
        when(user.getScreenName()).thenReturn("SomeScreenName");
        AccessToken accessToken = new AccessToken("veryImportantToken", "veryImportantTokenSecret");
        UserInfo userInfo = new UserInfoBuilder().id(777L).login("login").password("password").role(UserRole.ADMIN).build();

        TwitterUser expected = new TwitterUser();
        expected.setSocialId(12345L);
        expected.setName("SomeScreenName");
        expected.setToken("veryImportantToken");
        expected.setTokenSecret("veryImportantTokenSecret");
        expected.setUserInfo(
                new UserInfoBuilder().id(777L).login("login").password("password").role(UserRole.ADMIN).build()
        );

        //When
        TwitterUser twitterUser = TwitterUserMapper.toEntity(user, accessToken, userInfo);

        //Then
        assertThat(twitterUser).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void toDto() {
        //Given
        TwitterUser twitterUser = TwitterUserBuilder.getInstance()
                .id(777L)
                .socialId(1234567L)
                .name("someName")
                .token("awesomeToken")
                .tokenSecret("awesomeTokenSecret")
                .build();
        TwitterUserDTO expected = new TwitterUserDTO();
        expected.setId(777L);
        expected.setSocialId(1234567L);
        expected.setName("someName");

        //When
        TwitterUserDTO twitterUserDTO = TwitterUserMapper.toDto(twitterUser);

        //Then
        assertThat(twitterUserDTO).isEqualToComparingFieldByField(expected);
    }
}