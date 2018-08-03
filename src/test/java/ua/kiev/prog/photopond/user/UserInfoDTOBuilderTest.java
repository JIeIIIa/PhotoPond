package ua.kiev.prog.photopond.user;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoDTOBuilderTest {

    @Test
    public void id() {
        //Given
        Long id = 777L;

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(id)
                .build();

        //Then
        Assertions.assertThat(userInfoDTO.getId())
                .isEqualTo(id);
    }

    @Test
    public void login() {
        //Given
        Long id = 777L;

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .id(id)
                .build();

        //Then
        Assertions.assertThat(userInfoDTO.getId())
                .isEqualTo(id);
    }

    @Test
    public void oldPassword() {
        //Given
        String oldPassword = "someOldPassword";

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .oldPassword(oldPassword)
                .build();

        //Then
        Assertions.assertThat(userInfoDTO.getOldPassword())
                .isEqualTo(oldPassword);
    }

    @Test
    public void password() {
        //Given
        String password = "awesomePassword";

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .password(password)
                .build();

        //Then
        Assertions.assertThat(userInfoDTO.getPassword())
                .isEqualTo(password);
    }

    @Test
    public void passwordConfirmation() {
        //Given
        String passwordConfirmation = "awesomePassword";

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .passwordConfirmation(passwordConfirmation)
                .build();

        //Then
        Assertions.assertThat(userInfoDTO.getPasswordConfirmation())
                .isEqualTo(passwordConfirmation);
    }

    @Test
    public void role() {
        //Given
        UserRole role = UserRole.ADMIN;

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .role(role)
                .build();

        //Then
        Assertions.assertThat(userInfoDTO.getRole())
                .isEqualTo(role);
    }
}