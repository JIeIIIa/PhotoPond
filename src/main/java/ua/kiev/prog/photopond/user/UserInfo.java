package ua.kiev.prog.photopond.user;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserInfo {
    private long id;

    @NotNull
    @Size(min = 4, max = 30)
    private String login;

    @NotNull
    @Size(min = 6, max = 30)
    private String password;

    private UserRole role;

    public UserInfo() {
        role = UserRole.USER;
    }

    public UserInfo(String login, String password) {
        this();
        this.login = login;
        this.password = password;
    }

    public UserInfo(String login, String password, UserRole role) {
        this(login, password);
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

    public boolean isDeactivated() {
        return role == UserRole.DEACTIVATED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (login != null ? !login.equals(userInfo.login) : userInfo.login != null) return false;
        return password != null ? password.equals(userInfo.password) : userInfo.password == null;
    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role.name() +
                '}';
    }
}
