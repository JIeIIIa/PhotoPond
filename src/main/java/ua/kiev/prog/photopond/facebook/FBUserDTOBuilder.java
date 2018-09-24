package ua.kiev.prog.photopond.facebook;

public class FBUserDTOBuilder {
    private String email;

    private String fbId;
    
    private String name;

    private FBUserDTOBuilder() {
    }

    public static FBUserDTOBuilder getInstance() {
        return new FBUserDTOBuilder();
    }

    public FBUserDTOBuilder email(String email) {
        this.email = email;

        return this;
    }

    public FBUserDTOBuilder fbId(String fbId) {
        this.fbId = fbId;

        return this;
    }

    public FBUserDTOBuilder name(String name) {
        this.name = name;

        return this;
    }
    
    public FBUserDTO build() {
        FBUserDTO fbUserDTO = new FBUserDTO();

        fbUserDTO.setEmail(email);
        fbUserDTO.setFbId(fbId);
        fbUserDTO.setName(name);

        return fbUserDTO;
    }
}
