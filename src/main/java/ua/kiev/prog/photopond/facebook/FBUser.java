package ua.kiev.prog.photopond.facebook;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ua.kiev.prog.photopond.user.UserInfo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "facebookUsers")
public class FBUser implements Serializable {

    private static final long serialVersionUID = -213762331914925297L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String fbId;

    @NotNull
    private String email;

    private String name;

    private String accessToken;

    private LocalDateTime tokenExpires;

    @NotNull
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserInfo userInfo;

    public FBUser() {
        this.id = Long.MIN_VALUE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getTokenExpires() {
        return tokenExpires;
    }

    public void setTokenExpires(LocalDateTime tokenExpires) {
        this.tokenExpires = tokenExpires;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FBUser fbUser = (FBUser) o;
        return Objects.equals(id, fbUser.id) &&
                Objects.equals(fbId, fbUser.fbId) &&
                Objects.equals(email, fbUser.email) &&
                Objects.equals(name, fbUser.name) &&
                Objects.equals(accessToken, fbUser.accessToken) &&
                Objects.equals(tokenExpires, fbUser.tokenExpires) &&
                Objects.equals(userInfo, fbUser.userInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fbId, email, name, accessToken, tokenExpires, userInfo);
    }

    @Override
    public String toString() {
        return "FBUser{" +
                "id=" + id +
                ", fbId='" + fbId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", tokenExpires=" + tokenExpires +
                ", userInfo=" + userInfo +
                '}';
    }
}
