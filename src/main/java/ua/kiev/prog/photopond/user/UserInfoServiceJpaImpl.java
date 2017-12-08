package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;

import java.util.List;

@Service
@DevOrProd
@Transactional(readOnly = true)
public class UserInfoServiceJpaImpl implements UserInfoService{
    private static Logger log = LogManager.getLogger(UserInfoServiceJpaImpl.class);

    private final UserInfoJpaRepository userInfoRepository;

    @Autowired
    public UserInfoServiceJpaImpl(UserInfoJpaRepository userInfoJpaRepository) {
        log.debug("Create instance of " + UserInfoServiceJpaImpl.class);
        this.userInfoRepository = userInfoJpaRepository;
    }


    @Override
    public UserInfo getUserByLogin(String login) {
        log.debug("Call getUserByLogin for [ login = '{}'] ", login);
        return userInfoRepository.findByLogin(login);
    }

    @Override
    @Transactional
    public void addUser(UserInfo user) {
        log.debug("Add user:  " + user);
        if (user == null) {
            return;
        }
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
        UserInfo deletedUser = userInfoRepository.findOne(id);
        if (deletedUser != null) {
            userInfoRepository.delete(deletedUser);
            return deletedUser;
        }
        return null;
    }

    @Override
    @Transactional
    public UserInfo update(UserInfo userInfo) {
        log.traceEntry("Update user with { id = '{}' ]", userInfo.getId());
        UserInfo updatedUser = userInfoRepository.findOne(userInfo.getId());
        if (updatedUser == null) {
            log.debug("Cannot find user with [ id = {} ] in Repository", userInfo.getId());
            return null;
        }
        long usersWithSameLogin = userInfoRepository.countByLoginAndIdNot(userInfo.getLogin(), userInfo.getId());
        log.trace("[ usersWithSameLogin = {} ]", usersWithSameLogin);
        if (usersWithSameLogin == 0) {
            updatedUser.copyFrom(userInfo);
            log.trace("Updated information:   " + updatedUser);
            return updatedUser;
        }
        log.warn("Information for user with [ id = '{}' ] wasn't updated", userInfo.getId());
        return null;
    }

    @Override
    public UserInfo getUserById(long id) {
        log.traceEntry("Get user by [ id = {} ]", id);
        return userInfoRepository.findOne(id);
    }
}
