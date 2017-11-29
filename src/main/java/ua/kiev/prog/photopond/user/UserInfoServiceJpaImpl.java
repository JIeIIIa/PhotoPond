package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;

import java.util.List;

@Service
@DevOrProd
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
        log.debug("call getUserByLogin for login = '" + login + "'");
        return userInfoRepository.findByLogin(login);
    }

    @Override
    public void addUser(UserInfo user) {
        log.debug("Add user:  " + user);
        userInfoRepository.save(user);
    }

    @Override
    public boolean existByLogin(String login) {
        log.debug("Is exist user with [login = '" + login + "']");
        UserInfo user = userInfoRepository.findByLogin(login);
        boolean existUser = (user == null);
        log.debug("Exists user by [login = " + login + "]  =  " + existUser);
        return existUser;
    }

    @Override
    public List<UserInfo> getAllUsers() {
        log.trace("Find all users");
        return userInfoRepository.findAll();
    }

    @Override
    public UserInfo delete(long id) {
        log.debug("Delete user with [id = " + id + "]");
        UserInfo deletedUser = userInfoRepository.findOne(id);
        if (deletedUser != null) {
            userInfoRepository.delete(deletedUser);
            return deletedUser;
        }
        return null;
    }

    @Override
    public UserInfo update(UserInfo userInfo) {
        log.trace("Update user with {id = " + userInfo.getId() + "]");
        UserInfo checkedLoginUser = userInfoRepository.findByLogin(userInfo.getLogin());
        boolean notExistsUserWithSameLogin = (checkedLoginUser == null || checkedLoginUser.getId() == userInfo.getId());
        if (notExistsUserWithSameLogin) {
            UserInfo updatedUser = userInfoRepository.findOne(userInfo.getId());
            updatedUser.copyFrom(userInfo);
            log.trace("Updated information:   " + updatedUser);
            return updatedUser;
        }
        log.trace("Information for user with [id = " + userInfo.getId() + "] wasn't updated");
        return null;
    }

    @Override
    public UserInfo getUserById(long id) {
        log.trace("Get user by [id = " + id + "]");
        return userInfoRepository.findOne(id);
    }
}
