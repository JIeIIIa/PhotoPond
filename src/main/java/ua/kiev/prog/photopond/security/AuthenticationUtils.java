package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;

public class AuthenticationUtils {

    private static final Logger LOG = LogManager.getLogger(AuthenticationUtils.class);

    public static void login(String login, String role, HttpServletRequest request) {
        LOG.traceEntry("Try to log in for user [ login = '{}' ]", login);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, null,
                singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(token);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        LOG.debug("User [ login = '{}' ] was logged in.", login);
    }

    public static View userHomeRedirectView(String login) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/user/{login}/drive")
                .build()
                .expand(login)
                .encode();
        LOG.debug("Was built RedirectView to user home page: {}", uriComponents.getPath());

        return new RedirectView(uriComponents.toUriString(), true, true, false);
    }

}
