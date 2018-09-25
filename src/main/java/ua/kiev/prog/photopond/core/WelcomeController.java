package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {
    private static Logger log = LogManager.getLogger(WelcomeController.class);

    @RequestMapping(value = {"/", "index"})
    public String index() {
        log.debug("Request to \"/\"");
        return "index";
    }

    @RequestMapping("/about")
    public String about() {
        log.traceEntry("Request to about page");
        return "about";
    }

    @RequestMapping("/public/privacyPolicy")
    public String privacyPolicy() {
        log.traceEntry("Request to privacy policy page");
        return "privacyPolicy";
    }
}
