package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger LOG = LogManager.getLogger(UserDetailsServiceImpl.class);

    private final UserInfoJpaRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserInfoJpaRepository userRepository) {
        LOG.info("Create instance of " + UserDetailsServiceImpl.class);
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        LOG.debug("try load user by login: " + login);
        Optional<UserInfo> user = userRepository.findByLogin(login);

        UserDetails userDetails = user.map(this::convert)
                .orElseThrow(() -> {
                    LOG.warn("Cannot load UserInfo for [ login = '{}' ]", login);
                    return new UsernameNotFoundException(login + " not found");
                });

        LOG.debug("User [ login = {} ] was loaded", login);
        return userDetails;
    }

    private UserDetails convert(UserInfo user) {
        return User.withUsername(user.getLogin())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(user.isDeactivated())
                .build();
    }
}
