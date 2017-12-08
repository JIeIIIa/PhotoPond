package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ua.kiev.prog.photopond.exception.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static ua.kiev.prog.photopond.Utils.Utils.urlDecode;

public class AccessUserDirectoryInterceptor extends HandlerInterceptorAdapter {
    private static Logger log = LogManager.getLogger(AccessUserDirectoryInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.trace("Begin verifying access to user's directory.");
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String loginParameter = (String) pathVariables.get("login");
        String authorizedUsername = getUserLoginFromSecurityContext();
        log.trace("Values:   [ loginParameter = '{}' ]   and   [ authorizedUsername = '{}']", loginParameter, authorizedUsername);

        checkLogin(request, loginParameter, authorizedUsername);
        return true;
    }

    private void checkLogin(HttpServletRequest request, String loginParameter, String authorizedUsername) throws UnsupportedEncodingException,
            AccessDeniedException {
        if (loginParameter == null || !loginParameter.equals(authorizedUsername)) {
            String url = urlDecode(request.getRequestURL().toString());
            String uri = urlDecode(request.getRequestURI());

            StringBuilder sb = new StringBuilder()
                    .append("User '").append(authorizedUsername).append("'")
                    .append(" does not have access to url: ").append(url)
                    .append("      URI = '").append(uri).append("'")
                    .append(" does not start with '/user/").append(authorizedUsername).append("'");
            log.warn(sb.toString());

            addSessionAttribute(request, authorizedUsername, url);
            throw new AccessDeniedException(sb.toString());
        }
    }

    private String getUserLoginFromSecurityContext() {
        log.trace("Try to get login from SecurityContext");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    private void addSessionAttribute(HttpServletRequest request, String login, String url) {
        log.traceEntry("[ login = '{}' ],   [ url = '{}' ]", login, url);
        request.setAttribute("userLogin", login);
        request.setAttribute("url", url);

        log.trace("Attributes were added to request:   [ userLogin = '{}' ],   [ url = '{}'", login, url);
    }
}
