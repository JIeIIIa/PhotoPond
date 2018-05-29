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
public class UserInfoServiceJpaImpl implements UserInfoService{
    private static Logger log = LogManager.getLogger(UserInfoServiceJpaImpl.class);

    private final UserInfoJpaRepository userInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoServiceJpaImpl(UserInfoJpaRepository userInfoJpaRepository, BCryptPasswordEncoder passwordEncoder) {
        log.debug("Create instance of " + UserInfoServiceJpaImpl.class);
        this.userInfoRepository = userInfoJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserInfo getUserByLogin(String login) {
        log.debug("Call getUserByLogin for [ login = '{}'] ", login);
        UserInfo user = userInfoRepository.findByLogin(login);
        return user;
    }

    @Override
    @Transactional
    public void addUser(UserInfo user) {
        log.debug("Add user:  " + user);
        if (user == null) {
            return;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userInfoRepository.save(user);
    }

    @Override
    public boolean existByLogin(String login) {
        log.traceEntry("Is exist user with [ login = '{}' ]", login);
        UserInfo user = userInfoRepository.findByLogin(login);
        boolean existUser = (user != null);
        log.debug("Exists user by [ login = '{}' ]   =   {}", login, existUser);
        return existUser;
    }

    @Override
    public List<UserInfo> getAllUsers() {
        log.traceEntry("Find all users");
        return userInfoRepository.findAll();
    }

    @Override
    @Transactional
    public UserInfo delete(long id) {
        log.traceEntry("Delete user with [ id = '{}' ]", id);
        Optional<UserInfo> deletedUser = userInfoRepository.findById(id);
        if (deletedUser.isPresent()) {
            userInfoRepository.delete(deletedUser.get());
            return deletedUser.get();
        }
        return null;
    }

    @Override
    @Transactional
    public UserInfo update(UserInfo userInfo) {
        log.traceEntry("Update user with { id = '{}' ]", userInfo.getId());
        Optional<UserInfo> updatedUser = userInfoRepository.findById(userInfo.getId());
        if (!updatedUser.isPresent()) {
            log.debug("Cannot find user with [ id = {} ] in Repository", userInfo.getId());
            return null;
        }
        long usersWithSameLogin = userInfoRepository.countByLoginAndIdNot(userInfo.getLogin(), userInfo.getId());
        log.trace("[ usersWithSameLogin = {} ]", usersWithSameLogin);
        if (usersWithSameLogin == 0) {
            updatedUser.get().copyFrom(userInfo);
            log.trace("Updated information:   " + updatedUser);
            return updatedUser.get();
        }
        log.warn("Information for user with [ id = '{}' ] wasn't updated", userInfo.getId());
        return null;
    }

    @Override
    public Optional<UserInfo> getUserById(long id) {
        log.traceEntry("Get user by [ id = {} ]", id);
        return userInfoRepository.findById(id);
    }
}
