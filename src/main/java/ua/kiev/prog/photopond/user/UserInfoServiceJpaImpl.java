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

import static java.util.Objects.nonNull;

@Service
@DevOrProd
@Transactional(readOnly = true)
public class UserInfoServiceJpaImpl implements UserInfoService {
    private static final Logger LOG = LogManager.getLogger(UserInfoServiceJpaImpl.class);

    private final UserInfoJpaRepository userInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoServiceJpaImpl(UserInfoJpaRepository userInfoJpaRepository, BCryptPasswordEncoder passwordEncoder) {
        LOG.info("Create instance of {}", UserInfoServiceJpaImpl.class);
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
        if (nonNull(user) && nonNull(password)) {
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
    public Optional<UserInfo> findById(long id) {
        LOG.traceEntry("Get user by [ id = {} ]", id);
        return userInfoRepository.findById(id);
    }

    @Override
    @Transactional
    public boolean setNewPassword(UserInfoDTO userInfoDTO) {
        return userInfoRepository.findByLogin(userInfoDTO.getLogin())
                .filter(u -> passwordEncoder.matches(userInfoDTO.getOldPassword(), u.getPassword()))
                .map(u -> updatePassword(u, userInfoDTO.getPassword()))
                .orElse(false);

    }

    @Override
    public boolean resetPassword(String login, String password) {
        return userInfoRepository.findByLogin(login)
                .map(u -> updatePassword(u, password))
                .orElse(false);
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
