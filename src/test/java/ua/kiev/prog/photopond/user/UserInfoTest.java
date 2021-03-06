package ua.kiev.prog.photopond.user;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserInfoTest {

    @Test
    public void copyFromWithNotNullPassword() {
        //Given
        UserInfo user = new UserInfo("login", "password", UserRole.USER);
        user.setId(123L);
        UserInfo newInformation = new UserInfo("superMan", "qwerty", UserRole.ADMIN);
        user.setId(777L);
        UserInfo expected = new UserInfo("superMan", "qwerty", UserRole.ADMIN);
        user.setId(777L);

        //When
        user.copyFrom(newInformation);

        //Then
        assertThat(user)
                .isEqualTo(expected);
    }

    @Test
    public void copyFromWithNullPassword() {
        //Given
        UserInfo user = new UserInfo("login", "password", UserRole.USER);
        user.setId(123L);
        UserInfo newInformation = new UserInfo("superMan", null, UserRole.ADMIN);
        user.setId(777L);
        UserInfo expected = new UserInfo("superMan", "password", UserRole.ADMIN);
        user.setId(777L);

        //When
        user.copyFrom(newInformation);

        //Then
        assertThat(user)
                .isEqualTo(expected);
    }

    @Test
    void equals() {
        EqualsVerifier.forClass(UserInfo.class)
                .usingGetClass()
                .withIgnoredFields("role", "avatar")
                .verify();
    }
}