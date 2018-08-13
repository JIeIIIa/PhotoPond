package ua.kiev.prog.photopond.user;

import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.transfer.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;

@EqualPasswords(
        groups = {New.class, ChangePassword.class}, message = "{EqualPasswords}")
public class UserInfoDTO implements ConfirmedPassword {
    private Long id;

    @NotNull(message = "{NotNull.login}", groups = {New.class, Exist.class})
    @Size(min = 4, max = 30, message = "{Size.login}",
            groups = {New.class, Exist.class})
    @UniqueLogin(groups = New.class)
    private String login;

    @NotNull(message = "{NotNull.oldPassword}", groups = {ChangePassword.class})
    @Size(min = 6, max = 65, message = "{Size.password}",
            groups = {ChangePassword.class})
    private String oldPassword;

    @NotNull(message = "{NotNull.password}", groups = {New.class, ChangePassword.class})
    @Size(min = 6, max = 65, message = "{Size.password}",
            groups = {New.class, ChangePassword.class})
    private String password;

    private String passwordConfirmation;

    @NotNull(message = "{NotNull.role}", groups = {AdminEditing.class})
    private UserRole role;

    @NotNull(message = "NotNull.avatar", groups = {ChangeAvatar.class})
    private MultipartFile avatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public byte[] getAvatarAsBytes() {
        try {
            return avatar.getBytes();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "UserInfoDTO{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", role=" + role +
                '}';
    }
}
