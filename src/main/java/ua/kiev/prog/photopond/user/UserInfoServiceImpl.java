package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    private static Logger log = LogManager.getLogger(UserInfoServiceImpl.class);

    @Autowired
    private UserInfoSimpleRepository userInfoRepository;

    @Override
    public UserInfo getUserByLogin(String login) {
        log.debug("call getUserByLogin for login = '" + login + "'");
        return userInfoRepository.findByLogin(login);
    }

    @Override
    public void addUser(UserInfo user) {
        log.debug("call addUser:  " + user);
        userInfoRepository.addUser(user);
    }

    @Override
    public boolean existByLogin(String login) {
        log.debug("call existByLogin for login = '" + login + "'");
        boolean existUser = userInfoRepository.existByLogin(login);
        log.debug("");
        return existUser;
    }
}
