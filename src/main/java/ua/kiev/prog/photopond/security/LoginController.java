package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ua.kiev.prog.photopond.core.BindingErrorResolver;

import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Objects;

import static ua.kiev.prog.photopond.security.AuthenticationUtils.userHomeRedirectView;

@Controller
public class LoginController {
    private static final String SPRING_SECURITY_LAST_EXCEPTION = "SPRING_SECURITY_LAST_EXCEPTION";
    private static final String SECURITY_LAST_EXCEPTION_MESSAGE_MODEL_ATTRIBUTE = "securityLastExceptionMessage";

    private static Logger log = LogManager.getLogger(LoginController.class);

    final
    BindingErrorResolver bindingErrorResolver;

    @Autowired
    public LoginController(BindingErrorResolver bindingErrorResolver) {
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @RequestMapping("/login")
    public ModelAndView login(HttpSession session,
                              Locale locale) {
        log.debug("Get request to /login");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");

        Object securityLastException = session.getAttribute(SPRING_SECURITY_LAST_EXCEPTION);
        if (Objects.nonNull(securityLastException)) {
            String message = bindingErrorResolver.resolveMessage(securityLastException.getClass().getName(), locale);
            modelAndView.addObject(SECURITY_LAST_EXCEPTION_MESSAGE_MODEL_ATTRIBUTE, message);
            session.removeAttribute(SPRING_SECURITY_LAST_EXCEPTION);
        }
        return modelAndView;
    }

    @RequestMapping("/authorized")
    public ModelAndView authorized(Authentication authentication) {
        log.traceEntry();
        String login = authentication.getName();

        ModelAndView modelAndView = new ModelAndView(userHomeRedirectView(login));
        log.debug("User '{}' is authorized", login);

        return modelAndView;
    }
}
