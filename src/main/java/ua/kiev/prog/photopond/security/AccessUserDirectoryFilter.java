package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.kiev.prog.photopond.exception.AccessDeniedException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AccessUserDirectoryFilter implements Filter {
    private static Logger log = LogManager.getLogger(AccessUserDirectoryFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.debug("Filtering request");
        String userLogin = getUserLogin();

        startUrlContainsLogin((HttpServletRequest) servletRequest, userLogin);
        /*try {
        } catch (AccessDeniedException e) {
            log.debug("Access denied for " + userLogin);
            throw new ServletException("URL access denied", e);
        }*/

        filterChain.doFilter(servletRequest, servletResponse);

    }

    private String getUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    private void startUrlContainsLogin(HttpServletRequest request, String login) throws AccessDeniedException{
        String url = request.getRequestURL().toString();
        String uri = request.getRequestURI();//url.replace(request.getRequestURI(), request.getContextPath());
        log.debug("URI = " + uri + "    login = " + login);
        if (uri.startsWith("/user/")) {
            if(!uri.startsWith("/user/" + login + "/")) {
                log.debug("URI = '" + uri + "' does not start with '/user/" + login + "'");
                throw new AccessDeniedException("User '" + login + "' does not have access to url: " + url);
            }
            log.debug("Ok!    URI = '" + uri + "' starts with '/user/" + login + "'");
        }
    }

    @Override
    public void destroy() {

    }
}
