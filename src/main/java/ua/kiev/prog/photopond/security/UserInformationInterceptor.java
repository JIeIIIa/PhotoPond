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

public class UserInformationInterceptor extends HandlerInterceptorAdapter {
    private static Logger log = LogManager.getLogger(UserInformationInterceptor.class);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.trace("postHandle ->  method = " + request.getMethod() + "   URI = " + request.getRequestURI());
        if (isNotNeededAddAttribute(request, modelAndView)) {
            log.trace("postHandle ->  doesn't need to add attribute");
            return;
        }
        log.trace("postHandle ->  modelAndView not null and request uri doesn't start with 'redirect:'");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isUserAuthorized(authentication)) {
            log.trace("postHandle ->  userLogin attribute was added");
            String userLogin = authentication.getName();
            modelAndView.addObject("userLogin", userLogin);
        }
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
