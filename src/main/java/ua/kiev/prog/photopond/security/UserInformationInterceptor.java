package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ua.kiev.prog.photopond.Utils.Utils.urlDecode;

public class UserInformationInterceptor extends HandlerInterceptorAdapter {
    private static Logger log = LogManager.getLogger(UserInformationInterceptor.class);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.traceEntry("[ Method = '{}' ],   [ URI = '{}' ]", request.getMethod(), urlDecode(request.getRequestURI()));
        if (isNotNeededAddAttribute(request, modelAndView)) {
            log.trace("Doesn't need to add attribute");
            return;
        }

        log.trace("ModelAndView not null and request uri doesn't start with 'redirect:'");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isUserAuthorized(authentication)) {
            String userLogin = authentication.getName();
            modelAndView.addObject("userLogin", userLogin);
            log.debug("Attribute was added: [ userLogin = '{}' ]", userLogin);
        }
        log.traceExit();
    }

    private boolean isNotNeededAddAttribute(HttpServletRequest request, ModelAndView modelAndView) {
        String method = request.getMethod();
        return request.getRequestURI().startsWith("redirect:")
                || !RequestMethod.GET.name().equals(method)
                || modelAndView == null;
    }

    private boolean isUserAuthorized(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
