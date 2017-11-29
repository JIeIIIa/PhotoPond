package ua.kiev.prog.photopond.user;

public class UserInfoBuilder {
    private UserInfo userInfo = new UserInfo("", "", UserRole.USER);

    public UserInfoBuilder id(long id) {
        userInfo.setId(id);
        return this;
    }

    public UserInfoBuilder login(String login) {
        userInfo.setLogin(login);
        return this;
    }

    public UserInfoBuilder password(String password) {
        userInfo.setPassword(password);
        return this;
    }

    public UserInfoBuilder role(UserRole role) {
        userInfo.setRole(role);
        return this;
    }

    public UserInfo build() {
        return userInfo;
    }
}
