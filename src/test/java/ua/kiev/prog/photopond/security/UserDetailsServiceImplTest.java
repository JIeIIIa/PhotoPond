package ua.kiev.prog.photopond.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserInfoService userInfoService;

    private UserDetailsService userDetailsService;

    @Before
    public void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userInfoService);
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
        when(userInfoService.getUserByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        SimpleGrantedAuthority checkedAuthority = new SimpleGrantedAuthority(user.getRole().toString().toUpperCase());

        verify(userInfoService).getUserByLogin(eq(user.getLogin()));
        assertThat(compare(user, userDetails)).isTrue();
        assertThat(authorities.size()).isEqualTo(1);
        assertThat(authorities.contains(checkedAuthority)).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test(expected = UsernameNotFoundException.class)
    public void userNotFound() {
        when(userInfoService.getUserByLogin("someLogin"))
                .thenReturn(Optional.empty());

        userDetailsService.loadUserByUsername("someLogin");
    }

    @Test
    public void disabledUser() {
        UserInfo user = new UserInfo("disabledUser", "password", UserRole.DEACTIVATED);
        when(userInfoService.getUserByLogin(user.getLogin()))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());

        assertThat(compare(user, userDetails)).isTrue();
        assertThat(userDetails.isEnabled()).isFalse();
    }
}