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
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LoginController {
    private static Logger log = LogManager.getLogger(LoginController.class);

    @RequestMapping("/login")
    public String login() {
        log.debug("Request to /login");
        return "login";
    }

    @RequestMapping("/authorized")
    public ModelAndView authorized(Authentication authentication) {
        String login = authentication.getName();
        String redirectedUrl = "/user/" + login + "/";
        RedirectView redirectView = new RedirectView(redirectedUrl, true, true, false);
        ModelAndView modelAndView = new ModelAndView(redirectView);
        modelAndView.setStatus(HttpStatus.PERMANENT_REDIRECT);
        log.debug("User '" + login + " is authorized and redirected to " + redirectView.getUrl());
        return modelAndView;
    }


    @RequestMapping(value = "/user/{login}", method = RequestMethod.GET)
    @ResponseBody
    public String userHomePage(@PathVariable("login") String login, Authentication authentication) {
        String authLogin = authentication.getName();
        String answer;
        if (authLogin.equals(login)) {
            answer = "<html>This is '" + login + "' user. Congratulation! <a href=\"/\">Start page</a></html>";
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

    @RequestMapping("/accessDenied")
    public String accessDenied() {
        return "/errors/accessDenied";
    }
}
