package ua.kiev.prog.photopond.user;

import java.util.Objects;

public class UserPasswordDTO {
    private String login;

    private String oldPassword;

    private String newPassword;

    private String confirmNewPassword;

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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPasswordDTO that = (UserPasswordDTO) o;
        return Objects.equals(login, that.login) &&
                Objects.equals(oldPassword, that.oldPassword) &&
                Objects.equals(newPassword, that.newPassword) &&
                Objects.equals(confirmNewPassword, that.confirmNewPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, oldPassword, newPassword, confirmNewPassword);
    }

    @Override
    public String toString() {
        return "UserPasswordDTO{" +
                "login='" + login + '\'' +
                ", oldPassword='" + oldPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", confirmNewPassword='" + confirmNewPassword + '\'' +
                '}';
    }
}
