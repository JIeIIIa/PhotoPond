package ua.kiev.prog.photopond.user;

import org.springframework.web.multipart.MultipartFile;

public class UserInfoDTOBuilder {
    private Long id;

    private String login;

    private String oldPassword;

    private String password;

    private String passwordConfirmation;

    private UserRole role;

    private MultipartFile avatar;

    private UserInfoDTOBuilder() {
        role = UserRole.USER;
    }
    
    public static UserInfoDTOBuilder getInstance() {
        return new UserInfoDTOBuilder();
    }

    public UserInfoDTOBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public UserInfoDTOBuilder login(String login) {
        this.login = login;
        return this;
    }

    public UserInfoDTOBuilder oldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
        return this;
    }

    public UserInfoDTOBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserInfoDTOBuilder passwordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
        return this;
    }

    public UserInfoDTOBuilder role(UserRole role) {
        this.role = role;
        return this;
    }

    public UserInfoDTOBuilder avatar(MultipartFile avatar) {
        this.avatar = avatar;
        return this;
    }

    public UserInfoDTO build() {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(id);
        userInfoDTO.setLogin(login);
        userInfoDTO.setOldPassword(oldPassword);
        userInfoDTO.setPassword(password);
        userInfoDTO.setPasswordConfirmation(passwordConfirmation);
        userInfoDTO.setRole(role);
        userInfoDTO.setAvatar(avatar);

        return userInfoDTO;
    }
}
