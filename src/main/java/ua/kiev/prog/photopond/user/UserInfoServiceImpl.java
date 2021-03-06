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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

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
    public Optional<UserInfoDTO> findUserByLogin(String login) {
        LOG.debug("login = '{}'", login);
        return userInfoRepository.findUserByLogin(login)
                .map(UserInfoMapper::toDto);
    }

    @Override
    public void addUser(UserInfoDTO userDTO) {
        if (isNull(userDTO)) {
            LOG.warn("Try to add 'null' as user");
            return;
        }
        LOG.debug("Add user:  {}", userDTO.getLogin());
        try {
            UserInfo user = new UserInfoBuilder()
                    .login(userDTO.getLogin())
                    .role(userDTO.getRole())
                    .avatar(userDTO.getAvatarAsBytes())
                    .build();
            cryptPassword(user, userDTO.getPassword());
            userInfoRepository.addUser(user);
        } catch (AddToRepositoryException e) {
            LOG.warn("AddToRepositoryException was caught");
            /*NOP*/
        }
    }

    private void cryptPassword(UserInfo user, String password) {
        LOG.debug("Crypt password for user {}", user.getLogin());
        if (nonNull(password)) {
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
    public List<UserInfoDTO> findAllUsers() {
        LOG.trace("Find all users");
        return userInfoRepository.findAllUsers()
                .stream()
                .map(UserInfoMapper::toDto)
                .collect(toList());
    }

    @Override
    public Optional<UserInfoDTO> delete(long id) {
        LOG.debug("Delete user with [id = " + id + "]");
        Optional<UserInfo> deletedUser = userInfoRepository.findById(id);
        deletedUser.ifPresent(u -> userInfoRepository.delete(u.getId()));

        return LOG.traceExit(deletedUser.map(UserInfoMapper::toDto));
    }

    @Override
    public Optional<UserInfoDTO> updateBaseInformation(UserInfoDTO userInfoDTO) {
        LOG.trace("Update user with {id = " + userInfoDTO.getId() + "]");
        Optional<UserInfo> userById = userInfoRepository.findById(userInfoDTO.getId());
        if (!userById.isPresent()) {
            LOG.debug("User with id = {} not found", userInfoDTO.getId());
            return Optional.empty();
        }
        boolean existsUserWithSameLogin = userInfoRepository.existsByLogin(userInfoDTO.getLogin(), userInfoDTO.getId());
        LOG.trace("[ existsUserWithSameLogin = {} ]", existsUserWithSameLogin);
        if (existsUserWithSameLogin) {
            LOG.debug("User with login = '{}' is already exists", userInfoDTO.getLogin());
            return Optional.empty();
        }
        UserInfo userInfo = userById.get();
        userInfo.setLogin(userInfoDTO.getLogin());
        userInfo.setRole(userInfoDTO.getRole());

        return Optional.ofNullable(UserInfoMapper.toDto(userInfoRepository.update(userInfo)));
    }

    @Override
    public Optional<UserInfoDTO> findById(long id) {
        LOG.trace("Get user by [id = " + id + "]");
        return LOG.traceExit(userInfoRepository.findById(id).map(UserInfoMapper::toDto));
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
    public List<UserInfoDTO> findAllByRole(UserRole role) {
        return LOG.traceExit(userInfoRepository.findAllByRole(role)
                .stream()
                .map(UserInfoMapper::toDto)
                .collect(toList()));
    }
}
