package ua.kiev.prog.photopond.security;

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
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    private static Logger log = LogManager.getLogger(UserDetailsServiceImpl.class);

    private final UserInfoService userService;

    @Autowired
    public UserDetailsServiceImpl(UserInfoService userService) {
        log.traceEntry("Create UserDetailsServiceImpl");
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.debug("try load user by login: " + login);
        Optional<UserInfo> user = userService.findUserByLogin(login);

        if (!user.isPresent()) {
            log.warn("Cannot load UserInfo for [ login = '{}' ]", login);
            throw new UsernameNotFoundException(login + " not found");
        }

        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(user.get().getRole().toString()));

        UserDetails userDetails = User.withUsername(user.get().getLogin())
                .password(user.get().getPassword())
                .roles(user.get().getRole().name())
                .disabled(user.get().isDeactivated())
                .build();
        log.info("User [ login = {} ] was loaded", login);
        return userDetails;
    }
}
