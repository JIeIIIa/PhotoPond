package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import ua.kiev.prog.photopond.annotation.profile.DevOrProd;

@Configuration
@DevOrProd
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
    public void configure(WebSecurity web) {
        log.debug("Configure WebSecurity: add ignoring paths");
        web.ignoring()
                .antMatchers("/css/**")
                .antMatchers("/min/css/**")
                .antMatchers("/libs/**")
                .antMatchers("/pic/**")
                .antMatchers("/js/**")
                .antMatchers("/min/js/**")
                .antMatchers("/login");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.debug("Configure HttpSecurity.");
        http.requiresChannel()
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                .requiresSecure();
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/", "/index", "/index.html", "/registration", "/registration.html",
                        "/about", "/about.html",
                        "/favicon.ico",
                        "/public/**").permitAll()
                .antMatchers("/css/**", "/libs/**", "/js/**", "/pic/**").permitAll()
                .antMatchers("/test/**", "/testingAccessDenied").hasAnyRole("USER", "ADMIN")
                .antMatchers("/files/**").hasAnyRole("USER", "ADMIN")
                .antMatchers("/administration/**").hasAnyRole("ADMIN")
                .anyRequest().authenticated()
                .and();
        http.exceptionHandling()
                .accessDeniedPage("/accessDenied.html")
                .and();
        http.formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/authorized.html", true)
                .failureUrl("/login")
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

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
