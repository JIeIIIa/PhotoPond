package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.kiev.prog.photopond.core.WelcomeController;

@Controller
public class SecurityController {
    private static Logger log = LogManager.getLogger(WelcomeController.class);

    @RequestMapping("/login")
    public String login() {
        log.debug("Request to /login");
        return "login";
    }
}
