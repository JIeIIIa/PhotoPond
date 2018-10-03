package ua.kiev.prog.photopond.twitter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ua.kiev.prog.photopond.user.UserInfo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "twitterUsers")
public class TwitterUser implements Serializable {

    private static final long serialVersionUID = 8184173759819497591L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long socialId;

    private String token;

    private String tokenSecret;

    private String name;

    @NotNull
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserInfo userInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSocialId() {
        return socialId;
    }

    public void setSocialId(Long socialId) {
        this.socialId = socialId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        TwitterUser that = (TwitterUser) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(socialId, that.socialId) &&
                Objects.equals(token, that.token) &&
                Objects.equals(tokenSecret, that.tokenSecret) &&
                Objects.equals(name, that.name) &&
                Objects.equals(userInfo, that.userInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, socialId, token, tokenSecret, name, userInfo);
    }

    @Override
    public String toString() {
        return "TwitterUser{" +
                "id=" + id +
                ", socialId=" + socialId +
                ", name='" + name + '\'' +
                ", userInfo{" +
                "id=" + userInfo.getId() +
                ", login=" + userInfo.getLogin() +
                "}}";
    }
}
