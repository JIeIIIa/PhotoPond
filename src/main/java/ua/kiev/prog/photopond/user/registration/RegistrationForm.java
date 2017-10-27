package ua.kiev.prog.photopond.user.registration;

import ua.kiev.prog.photopond.user.UserInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RegistrationForm {
    @Valid
    @NotNull
    private UserInfo userInfo;

    private String passwordConfirmation;

    public RegistrationForm() {
    }

    public RegistrationForm(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public RegistrationForm(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public RegistrationForm(UserInfo userInfo, String passwordConfirmation) {
        this.userInfo = userInfo;
        this.passwordConfirmation = passwordConfirmation;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegistrationForm that = (RegistrationForm) o;

        if (userInfo != null ? !userInfo.equals(that.userInfo) : that.userInfo != null) return false;
        return passwordConfirmation != null ? passwordConfirmation.equals(that.passwordConfirmation) : that.passwordConfirmation == null;
    }

    @Override
    public int hashCode() {
        int result = userInfo != null ? userInfo.hashCode() : 0;
        result = 31 * result + (passwordConfirmation != null ? passwordConfirmation.hashCode() : 0);
        return result;
    }
}
