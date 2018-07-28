package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user/{login}/settings")
public class UserSettingsController {
    public static final Logger LOG = LogManager.getLogger(UserSettingsController.class);

    @RequestMapping("")
    public String allSettings(@PathVariable("login") String login) {
        LOG.traceEntry("Settings for user = '{}'", login);

        return "/settings/settings";
    }

}
