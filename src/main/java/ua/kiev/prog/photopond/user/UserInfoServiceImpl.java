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
        if (user != null && password != null) {
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
    public Optional<UserInfo> setNewPassword(String login, String newPassword) {
        if (login == null) {
            LOG.warn("Try to change password for user with null login");
            return Optional.empty();
        }
        Optional<UserInfo> user = userInfoRepository.findUserByLogin(login);
        user.ifPresent(u -> cryptPassword(u, newPassword));
        LOG.debug("Password was changed");
        return user;
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
