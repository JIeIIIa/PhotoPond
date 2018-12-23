package ua.kiev.prog.photopond.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
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
        assertThat(userInfoDTO.getId())
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
        assertThat(userInfoDTO.getId())
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
        assertThat(userInfoDTO.getOldPassword())
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
        assertThat(userInfoDTO.getPassword())
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
        assertThat(userInfoDTO.getPasswordConfirmation())
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
        assertThat(userInfoDTO.getRole())
                .isEqualTo(role);
    }

    @Test
    public void avatar() {
        //Given
        MultipartFile avatarFile = new MockMultipartFile("avatar.jgp", new byte[]{1, 2, 3, 4, 5, 6, 7});
        MultipartFile expected = new MockMultipartFile("avatar.jgp", new byte[]{1, 2, 3, 4, 5, 6, 7});
        byte[] expectedAvatar = {1, 2, 3, 4, 5, 6, 7};

        //When
        UserInfoDTO userInfoDTO = UserInfoDTOBuilder.getInstance()
                .avatar(avatarFile)
                .build();

        //Then
        assertThat(userInfoDTO.getAvatarAsBytes())
                .isNotNull()
                .isEqualTo(expectedAvatar);
        assertThat(userInfoDTO.getAvatar())
                .isNotNull()
                .isEqualToComparingFieldByField(expected);
    }
}