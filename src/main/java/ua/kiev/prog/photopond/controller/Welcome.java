package ua.kiev.prog.photopond.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Welcome {
    private static Logger log = LogManager.getLogger(Welcome.class);

    @RequestMapping(value = {"/", "index"})
    public String index() {
        log.debug("Request to \"/\"");
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        log.debug("Request to /login");
        return "login";
    }
}
