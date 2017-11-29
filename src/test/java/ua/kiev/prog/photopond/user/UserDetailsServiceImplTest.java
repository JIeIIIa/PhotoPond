package ua.kiev.prog.photopond.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserInfoService userInfoService;

    private UserDetailsService userDetailsService;

    @Before
    public void setUp() throws Exception {
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
    public void successAuthenticate() throws Exception {
        UserInfo user = new UserInfo("user", "password", UserRole.USER);
        when(userInfoService.getUserByLogin(user.getLogin()))
                .thenReturn(user);

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
    public void userNotFound() throws Exception {
        when(userInfoService.getUserByLogin("someLogin"))
                .thenReturn(null);

        userDetailsService.loadUserByUsername("someLogin");
    }

    @Test
    public void disabledUser() throws Exception {
        UserInfo user = new UserInfo("disabledUser", "password", UserRole.DEACTIVATED);
        when(userInfoService.getUserByLogin(user.getLogin()))
                .thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());

        assertThat(compare(user, userDetails)).isTrue();
        assertThat(userDetails.isEnabled()).isFalse();
    }
}