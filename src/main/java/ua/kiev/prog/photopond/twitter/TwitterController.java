package ua.kiev.prog.photopond.twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import static ua.kiev.prog.photopond.twitter.TwitterConstants.ACCOUNT_VIEW_URL;
import static ua.kiev.prog.photopond.twitter.TwitterRequestMappingConstants.*;

@Controller
public class TwitterController {

    private static final Logger LOG = LogManager.getLogger(TwitterController.class);

    private final TwitterService twitterService;

    @Autowired
    public TwitterController(TwitterService twitterService) {
        LOG.info("Create instance of {}", TwitterController.class);

        this.twitterService = twitterService;
    }

    @RequestMapping(value = ACCOUNT_VIEW_URL)
    public ModelAndView associateList(ModelAndView modelAndView,
                                      Authentication authentication) {
        LOG.traceEntry("Request to {}", ACCOUNT_VIEW_URL);

        modelAndView.setViewName("twitter/associate");
        TwitterUserDTO twitterUserDTO = twitterService.findAccountByLogin(authentication.getName());
        modelAndView.addObject("twitterUserDTO", twitterUserDTO);

        return modelAndView;
    }

    @RequestMapping(value = ASSOCIATE_ACCOUNT_URL, method = RequestMethod.GET)
    public ModelAndView associateRedirect() {
        String authorizationUrl = twitterService.getAuthorizationUrl();
        LOG.trace("{} redirect to {}", ASSOCIATE_ACCOUNT_URL, authorizationUrl);

        return new ModelAndView(new RedirectView(authorizationUrl, true, true, false));
    }

    @RequestMapping(value = DISASSOCIATE_ACCOUNT_URL, method = RequestMethod.POST)
    public ModelAndView disassociateAccount(ModelAndView modelAndView,
                                            Authentication authentication) {
        LOG.traceEntry("Request to {}", DISASSOCIATE_ACCOUNT_URL);
        twitterService.disassociateAccount(authentication.getName());
        modelAndView.setView(new RedirectView(ACCOUNT_VIEW_URL));

        return modelAndView;
    }

    @RequestMapping(value = AUTHENTICATION_WITH_TWITTER_URL, method = RequestMethod.GET)
    public ModelAndView authorizationRedirect() {
        String loginUrl = twitterService.getAuthenticationUrl();
        LOG.trace("{} redirect to {}", AUTHENTICATION_WITH_TWITTER_URL, loginUrl);

        return new ModelAndView(new RedirectView(loginUrl, true, true));
    }

    @RequestMapping(value = "/user/{login}/twitter", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity testSendPicture(@PathVariable("login") String userLogin) {

        return ResponseEntity.ok(twitterService.testPostPicture(userLogin));
    }
}
