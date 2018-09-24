package ua.kiev.prog.photopond.facebook;

import java.util.Objects;

public class FBUserDTO {
    private String email;

    private String fbId;

    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FBUserDTO fbUserDTO = (FBUserDTO) o;
        return Objects.equals(email, fbUserDTO.email) &&
                Objects.equals(fbId, fbUserDTO.fbId) &&
                Objects.equals(name, fbUserDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, fbId, name);
    }

    @Override
    public String toString() {
        return "FBUserDTO{" +
                "email='" + email + '\'' +
                ", fbId='" + fbId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
