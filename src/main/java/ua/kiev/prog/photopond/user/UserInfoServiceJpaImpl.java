package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;

import java.util.List;
import java.util.Optional;

@Service
@DevOrProd
@Transactional(readOnly = true)
public class UserInfoServiceJpaImpl implements UserInfoService {
    private static final Logger LOG = LogManager.getLogger(UserInfoServiceJpaImpl.class);

    private final UserInfoJpaRepository userInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoServiceJpaImpl(UserInfoJpaRepository userInfoJpaRepository, BCryptPasswordEncoder passwordEncoder) {
        LOG.info("Create instance of " + UserInfoServiceJpaImpl.class);
        this.userInfoRepository = userInfoJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Optional<UserInfo> findUserByLogin(String login) {
        LOG.debug("login = '{}'", login);
        return LOG.traceExit(userInfoRepository.findByLogin(login));
    }

    @Override
    @Transactional
    public void addUser(UserInfo user) {
        LOG.debug("Add user:  " + user);
        if (user == null) {
            LOG.warn("Try to add null as user");
            return;
        }
        cryptPassword(user, user.getPassword());
        userInfoRepository.save(user);
        LOG.traceExit("User {} was saved", user);
    }

    private void cryptPassword(UserInfo user, String password) {
        LOG.debug("Crypt password for user {}", user.getLogin());
        if (user != null) {
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
    public List<UserInfo> findAllUsers() {
        LOG.traceEntry("Find all users");
        return userInfoRepository.findAll();
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
        Optional<UserInfo> updatedUser = userInfoRepository.findById(userInfo.getId());
        if (!updatedUser.isPresent()) {
            LOG.debug("Cannot find user with [ id = {} ] in Repository", userInfo.getId());
            return Optional.empty();
        }
        long countUsersWithSameLogin = userInfoRepository.countByLoginAndIdNot(userInfo.getLogin(), userInfo.getId());
        LOG.trace("[ countUsersWithSameLogin = {} ]", countUsersWithSameLogin);
        if (countUsersWithSameLogin > 0) {
            LOG.debug("User with login = '{}' is already exists", userInfo.getLogin());
            return Optional.empty();
        }
        updatedUser.get().copyFrom(userInfo);
        LOG.trace("Information after update:   {}", updatedUser);
        return updatedUser;

    }

    @Override
    public Optional<UserInfo> findById(long id) {
        LOG.traceEntry("Get user by [ id = {} ]", id);
        return userInfoRepository.findById(id);
    }

    @Override
    public Optional<UserInfo> setNewPassword(String login, String newPassword) {
        if (login == null) {
            LOG.warn("Try to change password for user with null login");
            return Optional.empty();
        }
        Optional<UserInfo> user = userInfoRepository.findByLogin(login);
        user.ifPresent(u -> {
            cryptPassword(u, newPassword);
            userInfoRepository.save(u);
        });
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
