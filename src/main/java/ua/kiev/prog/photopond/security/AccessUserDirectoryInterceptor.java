package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ua.kiev.prog.photopond.exception.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class AccessUserDirectoryInterceptor extends HandlerInterceptorAdapter {
    private static Logger log = LogManager.getLogger(AccessUserDirectoryInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.trace("preHandle ->   starts");
        String login = getUserLogin();
        startUrlContainsLogin(request, login);

        return true;
    }

    private String getUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userLogin = authentication.getName();
        log.trace("getUserLogin ->   userLogin = " + userLogin);
        return userLogin;
    }

    private void startUrlContainsLogin(HttpServletRequest request, String login) throws AccessDeniedException {
        String url = request.getRequestURL().toString();
        String uri = request.getRequestURI();
        log.debug("startUrlContainsLogin ->   URI = " + uri + "    login = " + login);
        if (uri.startsWith("/user/")) {
            if (notStartsWithOrNotEqual(login, uri)) {
                log.debug("startUrlContainsLogin ->   URI = '" + uri + "' does not start with '/user/" + login + "'");
                addSessionAttribute(request, login, url);
                throw new AccessDeniedException("User '" + login + "' does not have access to url: " + url);
            }
            log.debug("startUrlContainsLogin ->   Ok!    URI = '" + uri + "' starts with '/user/" + login + "'");
        }
    }

    private boolean notStartsWithOrNotEqual(String login, String uri) {
        String pattern = "/user/" + login;
        boolean notEqual = !uri.equals(pattern);
        boolean notStartWith = !uri.startsWith(pattern + "/");
        boolean result = notStartWith && notEqual;
        log.trace("notStartsWithOrNotEqual ->   notEquals = " + notEqual + "   " + "notStartWith " + notStartWith
                + "   result = " + result);
        return result;
    }


    private void addSessionAttribute(HttpServletRequest request, String login, String url) {
        HttpSession session = request.getSession();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("userLogin", login);
        attributes.put("url", url);

        session.setAttribute("attributeMap", attributes);
        log.trace("addSessionAttribute ->   attribute was added: " + attributes);
    }
}
