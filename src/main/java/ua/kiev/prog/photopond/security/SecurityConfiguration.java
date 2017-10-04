package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static Logger log = LogManager.getLogger(SecurityConfiguration.class);

    @Autowired
    public void registerGlobalAuthentication(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService) throws Exception {
        log.debug("Setup UserDetailsService in AuthenticationManagerBuilder");
        auth
                .userDetailsService(userDetailsService);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        log.debug("Configure WebSecurity: add ignoring paths");
        web.ignoring()
                .antMatchers("/css/**")
                .antMatchers("/libs/**")
                .antMatchers("/login");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.debug("Configure HttpSecurity.");
        super.configure(http);

        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/**").hasAnyRole("USER", "ADMIN")
                .and();
        http.exceptionHandling()
                .accessDeniedPage("/unauthorized")
                .and();
        http.formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .failureUrl("/login?error=true")
                .loginProcessingUrl("/j_spring_security_check")
                .usernameParameter("j_login")
                .passwordParameter("j_password")
                .permitAll()
                .and();
        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .invalidateHttpSession(true);
    }
}
