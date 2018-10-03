package ua.kiev.prog.photopond.twitter;

public class TwitterUserDTOBuilder {
    private Long id;

    private Long socialId;

    private String name;

    private TwitterUserDTOBuilder() {

    }

    public static TwitterUserDTOBuilder getInstance() {
        return new TwitterUserDTOBuilder();
    }

    public TwitterUserDTOBuilder id(Long id) {
        this.id = id;

        return this;
    }

    public TwitterUserDTOBuilder socialId(Long socialId) {
        this.socialId = socialId;

        return this;
    }

    public TwitterUserDTOBuilder name(String name) {
        this.name = name;

        return this;
    }

    public TwitterUserDTO build() {
        TwitterUserDTO twitterUserDTO = new TwitterUserDTO();

        twitterUserDTO.setId(id);
        twitterUserDTO.setSocialId(socialId);
        twitterUserDTO.setName(name);

        return twitterUserDTO;
    }
}
