package ua.kiev.prog.photopond.user;

public class UserInfoMapper {
    public static UserInfoDTO toDto(UserInfo userInfo) {
        return UserInfoDTOBuilder.getInstance()
                .id(userInfo.getId())
                .login(userInfo.getLogin())
                .password(userInfo.getPassword())
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
