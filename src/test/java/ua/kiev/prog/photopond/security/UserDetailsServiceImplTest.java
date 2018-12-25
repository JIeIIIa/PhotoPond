package ua.kiev.prog.photopond.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoJpaRepository;
import ua.kiev.prog.photopond.user.UserRole;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
public class UserDetailsServiceImplTest {
    @Mock
    private UserInfoJpaRepository userInfoRepository;

    private UserDetailsService userDetailsService;

    @BeforeEach
    public void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userInfoRepository);
    }

    private boolean compare(UserInfo that, UserDetails other) {
        if (that == null || other == null) {
            return false;
        }

        UserInfo otherUser = new UserInfo(other.getUsername(), other.getPassword());
        return that.equals(otherUser) && that.isDeactivated() != other.isEnabled();
    }

    @Test
    public void successAuthenticate() {
        UserInfo user = new UserInfo("user", "password", UserRole.USER);
        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        SimpleGrantedAuthority checkedAuthority = new SimpleGrantedAuthority(user.getRole().toString().toUpperCase());

        verify(userInfoRepository).findByLogin(eq(user.getLogin()));
        assertThat(compare(user, userDetails)).isTrue();
        assertThat(authorities.size()).isEqualTo(1);
        assertThat(authorities.contains(checkedAuthority)).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    public void userNotFound() {
        when(userInfoRepository.findByLogin("someLogin"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("someLogin")
        );
    }

    @Test
    public void disabledUser() {
        UserInfo user = new UserInfo("disabledUser", "password", UserRole.DEACTIVATED);
        when(userInfoRepository.findByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());

        assertThat(compare(user, userDetails)).isTrue();
        assertThat(userDetails.isEnabled()).isFalse();
    }
}