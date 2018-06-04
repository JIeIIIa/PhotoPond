package ua.kiev.prog.photopond.security;

import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import ua.kiev.prog.photopond.user.UserRole;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("securityWebAuthTestConfig")
public class SpringSecurityWebAuthenticationTestConfiguration {

    @Bean
    @Primary
    public AuthenticationManager createAuthenticationManager() {
        AuthenticationManager am = mock(AuthenticationManager.class);
        when(am.authenticate(any(Authentication.class))).thenAnswer(
                (Answer<Authentication>) invocation -> (Authentication) invocation.getArguments()[0]);

        return am;
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        BCryptPasswordEncoder encoder = encoder();

        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(UserRole.USER.toString()));
        User basicActiveUser = new User("userTest", encoder.encode("passwordTest"), roles);

        roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(UserRole.USER.toString()));
        roles.add(new SimpleGrantedAuthority(UserRole.ADMIN.toString()));
        User managerActiveUser = new User("adminTest", encoder.encode("adminTest"), roles);

        roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(UserRole.DEACTIVATED.toString()));
        User deactivatedUser = new User("deactivatedUser", encoder.encode("qwerty123!"),
                false, true, true, true, roles);

        return new InMemoryUserDetailsManager(Arrays.asList(
                basicActiveUser, managerActiveUser, deactivatedUser
        ));
    }

    @Bean
    @Primary
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}