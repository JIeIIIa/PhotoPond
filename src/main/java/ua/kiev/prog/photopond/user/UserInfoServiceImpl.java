package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@ConditionalOnMissingBean(UserInfoService.class)
public class UserInfoServiceImpl implements UserInfoService {
    private static final Logger LOG = LogManager.getLogger(UserInfoServiceImpl.class);

    private final UserInfoSimpleRepository userInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoServiceImpl(UserInfoSimpleRepository userInfoRepository, BCryptPasswordEncoder passwordEncoder) {
        LOG.info("Create instance of " + UserInfoServiceImpl.class);
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UserInfo> findUserByLogin(String login) {
        LOG.debug("login = '{}'", login);
        return userInfoRepository.findUserByLogin(login);
    }

    @Override
    public void addUser(UserInfo user) {
        LOG.debug("Add user:  " + user);
        if (user == null) {
            LOG.warn("Try to add null as user");
            return;
        }
        try {
            cryptPassword(user, user.getPassword());
            userInfoRepository.addUser(user);
        } catch (AddToRepositoryException e) {
            LOG.warn("AddToRepositoryException was caught");
            /*NOP*/
        }
    }

    private void cryptPassword(UserInfo user, String password) {
        LOG.debug("Crypt password for user {}", user.getLogin());
        if (nonNull(user) && nonNull(password)) {
            user.setPassword(passwordEncoder.encode(password));
        }
    }

    @Override
    public boolean existsByLogin(String login) {
        LOG.trace("Is exist user with [login = '" + login + "']");
        boolean existUser = userInfoRepository.existsByLogin(login);
        LOG.debug("Exists user by [login = " + login + "]  =  " + existUser);
        return existUser;
    }

    @Override
    public List<UserInfo> findAllUsers() {
        LOG.trace("Find all users");
        return userInfoRepository.findAllUsers();
    }

    @Override
    public Optional<UserInfo> delete(long id) {
        LOG.debug("Delete user with [id = " + id + "]");
        Optional<UserInfo> deletedUser = userInfoRepository.findById(id);
        deletedUser.ifPresent(u -> userInfoRepository.delete(u.getId()));

        return LOG.traceExit(deletedUser);
    }

    @Override
    public Optional<UserInfo> update(UserInfo userInfo) {
        LOG.trace("Update user with {id = " + userInfo.getId() + "]");
        Optional<UserInfo> userById = userInfoRepository.findById(userInfo.getId());
        if (!userById.isPresent()) {
            LOG.debug("User with id = {} not found", userInfo.getId());
            return Optional.empty();
        }
        boolean existsUserWithSameLogin = userInfoRepository.existsByLogin(userInfo.getLogin(), userInfo.getId());
        LOG.trace("[ existsUserWithSameLogin = {} ]", existsUserWithSameLogin);
        if (existsUserWithSameLogin) {
            LOG.debug("User with login = '{}' is already exists", userInfo.getLogin());
            return Optional.empty();
        }
        cryptPassword(userInfo, userInfo.getPassword());
        return Optional.ofNullable(userInfoRepository.update(userInfo));

    }

    @Override
    public Optional<UserInfo> findById(long id) {
        LOG.trace("Get user by [id = " + id + "]");
        return LOG.traceExit(userInfoRepository.findById(id));
    }

    @Override
    public boolean setNewPassword(UserInfoDTO userInfoDTO) {
        LOG.traceEntry("Try to set new password for '{}'", userInfoDTO.getLogin());
        return userInfoRepository.findUserByLogin(userInfoDTO.getLogin())
                .filter(u -> passwordEncoder.matches(userInfoDTO.getOldPassword(), u.getPassword()))
                .map(u -> updatePassword(u, userInfoDTO.getPassword()))
                .orElse(false);
    }

    @Override
    public boolean resetPassword(String login, String password) {
        LOG.traceEntry("Reset the password for '{}'", login);
        return userInfoRepository.findUserByLogin(login)
                .map(u -> updatePassword(u, password))
                .orElse(false);
    }

    @Override
    public boolean updateAvatar(UserInfoDTO userInfoDTO) {
        LOG.traceEntry("Update the avatar for '{}'", userInfoDTO.getLogin());
        return userInfoRepository.findUserByLogin(userInfoDTO.getLogin())
                .map(u -> {
                    u.setAvatar(userInfoDTO.getAvatarAsBytes());
                    return u;
                })
                .map(userInfoRepository::update).isPresent();
    }

    @Override
    public byte[] retrieveAvatar(String login) {
        LOG.traceEntry("Try to retrieve the avatar for '{}'", login);
        return userInfoRepository.findUserByLogin(login)
                .map(UserInfo::getAvatar)
                .orElseGet(() -> new byte[0]);
    }

    private Boolean updatePassword(UserInfo user, String password) {
        cryptPassword(user, password);
        userInfoRepository.update(user);
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
