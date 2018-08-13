package ua.kiev.prog.photopond.user;

public class UserInfoBuilder {
    private Long id = Long.MIN_VALUE;

    private String login = "";

    private String password = "";

    private UserRole role = UserRole.USER;

    private byte[] avatar;

    public UserInfoBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public UserInfoBuilder login(String login) {
        this.login = login;
        return this;
    }

    public UserInfoBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserInfoBuilder role(UserRole role) {
        this.role = role;
        return this;
    }

    public UserInfoBuilder avatar(byte[] avatar) {
        this.avatar = avatar;
        return this;
    }

    public UserInfo build() {
        UserInfo userInfo = new UserInfo(login, password, UserRole.USER);

        userInfo.setId(id);
        userInfo.setLogin(login);
        userInfo.setPassword(password);
        userInfo.setRole(role);
        userInfo.setAvatar(avatar);

        return userInfo;
    }
}
