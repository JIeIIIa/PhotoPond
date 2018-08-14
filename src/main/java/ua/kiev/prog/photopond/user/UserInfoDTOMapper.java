package ua.kiev.prog.photopond.user;

public class UserInfoDTOMapper {
    public static UserInfoDTO toDto(UserInfo userInfo) {
        return UserInfoDTOBuilder.getInstance()
                .login(userInfo.getLogin())
                .role(userInfo.getRole())
                .build();
    }

    public static void fromDto(UserInfoDTO userInfoDTO, UserInfo target) {
        target.setLogin(userInfoDTO.getLogin());
        target.setPassword(userInfoDTO.getPassword());
        target.setRole(userInfoDTO.getRole());
        target.setAvatar(userInfoDTO.getAvatarAsBytes());
    }

    public static UserInfo fromDto(UserInfoDTO userInfoDTO) {
        UserInfo userInfo = new UserInfo();
        fromDto(userInfoDTO, userInfo);
        return userInfo;
    }


}
