package ua.kiev.prog.photopond.user;

public class UserPasswordDTOBuilder {
    private String login;

    private String oldPassword;

    private String newPassword;

    private String confirmNewPassword;


    private UserPasswordDTOBuilder() {
    }

    static UserPasswordDTOBuilder getInstance() {
        return new UserPasswordDTOBuilder();
    }
    
    public UserPasswordDTOBuilder login(String login) {
        this.login = login;
        return this;
    }

    public UserPasswordDTOBuilder oldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
        return this;
    }

    public UserPasswordDTOBuilder newPassword(String newPassword) {
        this.newPassword = newPassword;
        return this;
    }

    public UserPasswordDTOBuilder confirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
        return this;
    }

    public UserPasswordDTO build() {
        UserPasswordDTO passwordDTO = new UserPasswordDTO();
        passwordDTO.setLogin(login);
        passwordDTO.setNewPassword(newPassword);
        passwordDTO.setOldPassword(oldPassword);
        passwordDTO.setConfirmNewPassword(confirmNewPassword);

        return passwordDTO;
    }
}
