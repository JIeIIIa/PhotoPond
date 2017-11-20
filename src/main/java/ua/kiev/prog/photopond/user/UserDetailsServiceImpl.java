package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    private static Logger log = LogManager.getLogger(UserDetailsServiceImpl.class);

    private final UserInfoService userService;

    @Autowired
    public UserDetailsServiceImpl(UserInfoService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.debug("try find user: " + login);
        UserInfo user = userService.getUserByLogin(login);

        if (user == null) {
            log.debug("login = " + login + "   userInfo = null");
            throw new UsernameNotFoundException(login + " not found");
        }

        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(user.getRole().toString()));
        return new User(user.getLogin(), user.getPassword(), roles);
    }
}
