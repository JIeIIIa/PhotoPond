package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static ua.kiev.prog.photopond.user.UserInfoMapper.fromDto;

@Service
@DevOrProd
@Transactional(readOnly = true)
public class UserInfoServiceJpaImpl implements UserInfoService {
    private static final Logger LOG = LogManager.getLogger(UserInfoServiceJpaImpl.class);
    private static final String DEFAULT_AVATAR_FILENAME = "defaultAvatar.png";

    private final UserInfoJpaRepository userInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private byte[] defaultAvatar = loadDefaultAvatar();

    @Autowired
    public UserInfoServiceJpaImpl(UserInfoJpaRepository userInfoJpaRepository, BCryptPasswordEncoder passwordEncoder) {
        LOG.info("Create instance of {}", UserInfoServiceJpaImpl.class);
        this.userInfoRepository = userInfoJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void setDefaultAvatar(byte[] defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    private byte[] loadDefaultAvatar() {
        ClassPathResource resource = new ClassPathResource(DEFAULT_AVATAR_FILENAME);
        try (InputStream is = resource.getInputStream()) {
            return LOG.traceExit("Default avatar was loaded from resources", toByteArray(is));
        } catch (IOException e) {
            LOG.warn("Failure loading default avatar from resources");
            return new byte[0];
        }
    }

    @Override
    public Optional<UserInfoDTO> findUserByLogin(String login) {
        LOG.debug("login = '{}'", login);
        return LOG.traceExit(userInfoRepository.findByLogin(login))
                .map(UserInfoMapper::toDto);
    }

    @Override
    @Transactional
    public void addUser(UserInfoDTO userDTO) {
        if (isNull(userDTO)) {
            LOG.warn("Try to add 'null' as user");
            return;
        }
        LOG.debug("Add user:  {}", userDTO.getLogin());
        UserInfo user = fromDto(userDTO);
        cryptPassword(user, user.getPassword());
        userInfoRepository.saveAndFlush(user);
        LOG.traceExit("User {} was saved", user);
    }

    private void cryptPassword(UserInfo user, String password) {
        LOG.debug("Crypt password for user {}", user.getLogin());
        if (nonNull(password)) {
            user.setPassword(passwordEncoder.encode(password));
        }
    }

    @Override
    public boolean existsByLogin(String login) {
        LOG.traceEntry("Is exist user with [ login = '{}' ]", login);
        Optional<UserInfo> user = userInfoRepository.findByLogin(login);
        LOG.debug("Exists user by [ login = '{}' ]   =   {}", login, user.isPresent());
        return user.isPresent();
    }

    @Override
    public List<UserInfoDTO> findAllUsers() {
        LOG.traceEntry("Find all users");
        return userInfoRepository.findAll()
                .stream()
                .map(UserInfoMapper::toDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public Optional<UserInfo> delete(long id) {
        LOG.traceEntry("Delete user with [ id = '{}' ]", id);
        Optional<UserInfo> deletedUser = userInfoRepository.findById(id);
        deletedUser.ifPresent(userInfoRepository::delete);

        return LOG.traceExit(deletedUser);
    }

    @Override
    @Transactional
    public Optional<UserInfo> update(UserInfo userInfo) {
        LOG.traceEntry("Update user with { id = '{}' ]", userInfo.getId());

        long countUsersWithSameLogin = userInfoRepository.countByLoginAndIdNot(userInfo.getLogin(), userInfo.getId());
        LOG.trace("[ countUsersWithSameLogin = {} ]", countUsersWithSameLogin);
        if (countUsersWithSameLogin > 0) {
            LOG.debug("There is {} user(-s) with login = '{}' already exists",
                    countUsersWithSameLogin,
                    userInfo.getLogin());
            return Optional.empty();
        }

        Optional<UserInfo> updated = userInfoRepository.findById(userInfo.getId())
                .map(u -> copyUserInfo(u, userInfo))
                .map(userInfoRepository::save);

        return LOG.traceExit("Information after update:   {}", updated);
    }

    private UserInfo copyUserInfo(UserInfo source, UserInfo newInformation) {
        cryptPassword(newInformation, newInformation.getPassword());
        source.copyFrom(newInformation);
        return source.copyFrom(newInformation);
    }


    @Override
    public Optional<UserInfoDTO> findById(long id) {
        LOG.traceEntry("Get user by [ id = {} ]", id);
        return userInfoRepository.findById(id).map(UserInfoMapper::toDto);
    }

    @Override
    @Transactional
    public boolean setNewPassword(UserInfoDTO userInfoDTO) {
        LOG.traceEntry("Try to set new password for '{}'", userInfoDTO.getLogin());
        return userInfoRepository.findByLogin(userInfoDTO.getLogin())
                .filter(u -> passwordEncoder.matches(userInfoDTO.getOldPassword(), u.getPassword()))
                .map(u -> updatePassword(u, userInfoDTO.getPassword()))
                .orElse(false);

    }

    @Override
    public boolean resetPassword(String login, String password) {
        LOG.traceEntry("Reset the password for '{}'", login);
        return userInfoRepository.findByLogin(login)
                .map(u -> updatePassword(u, password))
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean updateAvatar(UserInfoDTO userInfoDTO) {
        LOG.traceEntry("Update the avatar for '{}'", userInfoDTO.getLogin());
        return userInfoRepository.findByLogin(userInfoDTO.getLogin())
                .map(u -> {
                    u.setAvatar(userInfoDTO.getAvatarAsBytes());
                    return u;
                })
                .map(userInfoRepository::saveAndFlush)
                .isPresent();
    }

    @Override
    public byte[] retrieveAvatar(String login) {
        LOG.traceEntry("Try to retrieve the avatar for '{}'", login);
        return userInfoRepository.findByLogin(login)
                .map(UserInfo::getAvatar)
                .orElse(this.defaultAvatar);
    }

    private Boolean updatePassword(UserInfo userInfo, String password) {
        cryptPassword(userInfo, password);
        userInfoRepository.save(userInfo);
        LOG.debug("Password was changed");
        return true;
    }

    @Override
    public boolean existsWithAdminRole() {
        return LOG.traceExit(userInfoRepository.countByRole(UserRole.ADMIN) > 0);
    }

    @Override
    public List<UserInfo> findAllByRole(UserRole role) {
        return LOG.traceExit(userInfoRepository.findAllByRole(role));
    }
}
