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
    private static Logger log = LogManager.getLogger(UserInfoServiceImpl.class);

    private final UserInfoSimpleRepository userInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoServiceImpl(UserInfoSimpleRepository userInfoRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UserInfo> getUserByLogin(String login) {
        log.debug("call getUserByLogin for login = '" + login + "'");
        return userInfoRepository.findByLogin(login);
    }

    @Override
    public void addUser(UserInfo user) {
        log.debug("Add user:  " + user);
        if (user == null) {
            return;
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userInfoRepository.addUser(user);
        } catch (AddToRepositoryException e) {
            /*NOP*/
        }
    }

    @Override
    public boolean existByLogin(String login) {
        log.debug("Is exist user with [login = '" + login + "']");
        boolean existUser = userInfoRepository.existByLogin(login);
        log.debug("Exists user by [login = " + login + "]  =  " + existUser);
        return existUser;
    }

    @Override
    public List<UserInfo> getAllUsers() {
        log.trace("Find all users");
        return userInfoRepository.getAllUsers();
    }

    @Override
    public UserInfo delete(long id) {
        log.debug("Delete user with [id = " + id + "]");
        Optional<UserInfo> deletedUser = userInfoRepository.getUserById(id);
        if (deletedUser.isPresent()) {
            userInfoRepository.delete(id);
            return deletedUser.get();
        }
        return null;
    }

    @Override
    public UserInfo update(UserInfo userInfo) {
        log.trace("Update user with {id = " + userInfo.getId() + "]");
        boolean existsUserWithSameLogin = userInfoRepository.existByLogin(userInfo.getLogin(), userInfo.getId());
        if (existsUserWithSameLogin) {
            return null;
        }
        return userInfoRepository.update(userInfo);

    }

    @Override
    public Optional<UserInfo> getUserById(long id) {
        log.trace("Get user by [id = " + id + "]");
        return userInfoRepository.getUserById(id);
    }
}
