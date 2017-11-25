package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    private static Logger log = LogManager.getLogger(UserInfoServiceImpl.class);

    private final UserInfoSimpleRepository userInfoRepository;

    @Autowired
    public UserInfoServiceImpl(UserInfoSimpleRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public UserInfo getUserByLogin(String login) {
        log.debug("call getUserByLogin for login = '" + login + "'");
        return userInfoRepository.findByLogin(login);
    }

    @Override
    public void addUser(UserInfo user) {
        log.debug("Add user:  " + user);
        try {
            userInfoRepository.addUser(user);
        } catch (AddToRepositoryException e) {
            /*NOP*/
        }
    }

    @Override
    public boolean existByLogin(String login) {
        log.debug("Is exist user with [login = '" + login + "']");
        boolean existUser = userInfoRepository.existByLogin(login);
        log.debug("");
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
        return userInfoRepository.delete(id);
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
    public UserInfo getUserById(long id) {
        log.trace("Get user by [id = " + id + "]");
        return userInfoRepository.getUserById(id);
    }
}
