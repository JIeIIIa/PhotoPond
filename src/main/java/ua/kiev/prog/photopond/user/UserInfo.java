package ua.kiev.prog.photopond.user;

public class UserInfo {
    private long id;

    private String login;
    private String password;

    private UserRole role;

    public UserInfo() {
    }

    public UserInfo(String login, String password) {
        this.login = login;
        this.password = password;
        role = UserRole.USER;
    }

    public UserInfo(String login, String password, UserRole role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
