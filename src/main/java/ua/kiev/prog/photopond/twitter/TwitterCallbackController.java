package ua.kiev.prog.photopond.twitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.security.AuthenticationUtils;
import ua.kiev.prog.photopond.user.UserInfoDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static java.util.Objects.isNull;
import static ua.kiev.prog.photopond.twitter.TwitterRequestMappingConstants.*;
import static ua.kiev.prog.photopond.user.SettingsPageUtils.socials;

@Controller
public class TwitterCallbackController {
    private static final Logger LOG = LogManager.getLogger(TwitterCallbackController.class);

    private final TwitterService twitterService;

    private final BindingErrorResolver bindingErrorResolver;

    @Autowired
    public TwitterCallbackController(TwitterService twitterService, BindingErrorResolver bindingErrorResolver) {
        LOG.info("Create instance of class {}", this.getClass().getName());
        this.twitterService = twitterService;
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @RequestMapping(value = ASSOCIATE_CALLBACK_SHORT_URL, params = {"oauth_token", "oauth_verifier"},
            method = RequestMethod.GET)
    public ModelAndView associateAccount(@RequestParam("oauth_token") String oAuthToken,
                                         @RequestParam("oauth_verifier") String oAuthVerifier,
                                         Authentication authentication,
                                         Locale locale,
                                         RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Associate twitter account [oauth_token = {}]", oAuthToken);

        if (isNull(authentication) || !authentication.isAuthenticated()) {
            LOG.error("Unauthorized user request [oauth_token = {}]", oAuthToken);
            redirectAttributes.addFlashAttribute(
                    ERROR_ATTRIBUTE_NAME,
                    bindingErrorResolver.resolveMessage("twitter.error.unauthorizedAssociation", locale)
            );
            LOG.error("Unauthorized Twitter account association");
        } else {
            TwitterUserDTO twitterUserDTO = twitterService.associateAccount(authentication.getName(), oAuthToken, oAuthVerifier);
            LOG.trace(twitterUserDTO);
        }

        return socials(authentication, redirectAttributes);
    }

    @RequestMapping(value = ASSOCIATE_CALLBACK_SHORT_URL, params = {"denied"}, method = RequestMethod.GET)
    public ModelAndView associateAccount(@RequestParam("denied") String oAuthToken,
                                         Authentication authentication,
                                         Locale locale,
                                         RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Account associate failure [oauth_token = {}]", oAuthToken);

        twitterService.removeRequestToken(oAuthToken);

        ModelAndView modelAndView = socials(authentication, redirectAttributes);
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE_NAME, bindingErrorResolver.resolveMessage("twitter.error.associate", locale));

        return modelAndView;

    }

    @RequestMapping(value = LOGIN_CALLBACK_SHORT_URL, params = {"oauth_token", "oauth_verifier"},
            method = RequestMethod.GET)
    public ModelAndView authorization(@RequestParam("oauth_token") String oAuthToken,
                                      @RequestParam("oauth_verifier") String oAuthVerifier,
                                      HttpServletRequest request) {
        LOG.traceEntry("Authorize user [oauth_token = {}]", oAuthToken);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));

        UserInfoDTO userInfoDTO = twitterService.findUserInfoByRequestToken(oAuthToken, oAuthVerifier);

        AuthenticationUtils.login(userInfoDTO.getLogin(), userInfoDTO.getRole().toString(), request);
        modelAndView.setView(AuthenticationUtils.userHomeRedirectView(userInfoDTO.getLogin()));

        LOG.trace("Twitter user was authorized as [login = ]", userInfoDTO.getLogin());
        return modelAndView;
    }

    @RequestMapping(value = LOGIN_CALLBACK_SHORT_URL, params = {"denied"}, method = RequestMethod.GET)
    public ModelAndView authorizationError(@RequestParam("denied") String oAuthToken,
                                           Locale locale,
                                           RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Authorization failure [oauth_token = {}]", oAuthToken);

        twitterService.removeRequestToken(oAuthToken);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));
        redirectAttributes.addFlashAttribute(
                ERROR_AUTH_ATTRIBUTE_NAME,
                bindingErrorResolver.resolveMessage("twitter.error.authorization", locale)
        );

        return modelAndView;
    }
}
