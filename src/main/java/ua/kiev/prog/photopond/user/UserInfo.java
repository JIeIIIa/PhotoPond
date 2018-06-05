package ua.kiev.prog.photopond.user;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "usersInfo")
public class UserInfo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 4, max = 30)
    private String login;

    @NotNull
    @Size(min = 6, max = 65)
    private String password;

    @Enumerated(EnumType.ORDINAL)
    private UserRole role;

    public UserInfo() {
        role = UserRole.USER;
        id = Long.MIN_VALUE;
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

    @JsonIgnore
    public boolean isDeactivated() {
        return role == UserRole.DEACTIVATED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(login, userInfo.login) &&
                Objects.equals(password, userInfo.password);
    }

    @Override
    public int hashCode() {

        return Objects.hash(login, password);
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

    public UserInfo copyFrom(UserInfo userInfo) {
        id = userInfo.getId();
        login = userInfo.getLogin();
        password = userInfo.getPassword();
        role = userInfo.getRole();
        return this;
    }
}
