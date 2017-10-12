package ua.kiev.prog.photopond.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import ua.kiev.prog.photopond.user.UserRole;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@TestConfiguration
public class SpringSecurityWebAuthenticationTestConfiguration {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {

        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(UserRole.USER.toString()));
        User basicActiveUser = new User("userTest", "passwordTest", roles);

        roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(UserRole.USER.toString()));
        roles.add(new SimpleGrantedAuthority(UserRole.ADMIN.toString()));
        User managerActiveUser = new User("adminTest", "adminTest", roles);

        return new InMemoryUserDetailsManager(Arrays.<UserDetails>asList(
                basicActiveUser, managerActiveUser
        ));
    }
}