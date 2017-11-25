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
        log.debug("call addUser:  " + user);
        try {
            userInfoRepository.addUser(user);
        } catch (AddToRepositoryException e) {
            /*NOP*/
        }
    }

    @Override
    public boolean existByLogin(String login) {
        log.debug("call existByLogin for login = '" + login + "'");
        boolean existUser = userInfoRepository.existByLogin(login);
        log.debug("");
        return existUser;
    }

    @Override
    public List<UserInfo> getAllUsers() {
        log.debug("call getAllUsers");
        return userInfoRepository.getAllUsers();
    }

    @Override
    public UserInfo delete(long id) {
        log.debug("call delete(" + id + ")");
        return userInfoRepository.delete(id);
    }
}
