package ua.kiev.prog.photopond.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
    private static Logger log = LogManager.getLogger(LoginController.class);

    @RequestMapping("/login")
    public String login() {
        log.debug("Request to /login");
        return "login";
    }

    @RequestMapping("/authorized")
    public ModelAndView authorized(ModelAndView modelAndView, Authentication authentication) {
        String login = authentication.getName();
        String redirectedUrl = "redirect:/user/" + login + "/";
        modelAndView.setViewName(redirectedUrl);
        modelAndView.setStatus(HttpStatus.PERMANENT_REDIRECT);
        log.debug("User '" + login + " is authorized and redirected to " + redirectedUrl);
        return modelAndView;
    }

    @RequestMapping(value = "/user/{login}", method = RequestMethod.GET)
    @ResponseBody
    public String userHomePage(@PathVariable("login") String login, Authentication authentication) {
        String authLogin = authentication.getName();
        String answer;
        if (authLogin.equals(login)) {
            answer = "This is '" + login + "' user. Congratulation!";
        } else {
            answer = "Access denied!!!";
        }
        return answer;
    }

    @RequestMapping(value = "/user/{login}/test")
    @ResponseBody
    public String userTest(@PathVariable("login") String login) {
        return login + "   testing page";
    }

    @RequestMapping("/testingAccessDenied")
    @ResponseBody
    public String testingAccessDenied() {
        return "Access denied! /testingAccessDenied";
    }
}
