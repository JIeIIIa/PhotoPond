package ua.kiev.prog.photopond.facebook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.security.AuthenticationUtils;
import ua.kiev.prog.photopond.user.UserInfoDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static ua.kiev.prog.photopond.facebook.FBConstants.FB_CALLBACK_URL;
import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ERROR_ATTRIBUTE_NAME;
import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ERROR_AUTH_ATTRIBUTE_NAME;
import static ua.kiev.prog.photopond.user.SettingsPageUtils.socials;

@Controller
public class FBCallbackController {

    private static final Logger LOG = LogManager.getLogger(FBCallbackController.class);

    private final FBService fbService;

    private final BindingErrorResolver bindingErrorResolver;

    @Autowired
    public FBCallbackController(FBService fbService, BindingErrorResolver bindingErrorResolver) {
        LOG.info("Create instance of class {}", this.getClass().getName());
        this.fbService = fbService;
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @RequestMapping(value = FB_CALLBACK_URL, method = RequestMethod.GET, params = {"code", "state=ASSOCIATE"})
    @ResponseBody
    public ModelAndView associateAccount(@RequestParam("code") String code,
                                         @RequestParam("state") FBState state,
                                         Authentication authentication,
                                         Locale locale,
                                         RedirectAttributes redirectAttributes) {
        LOG.traceEntry("State = {}", state);

        if (!authentication.isAuthenticated()) {
            LOG.error("Unauthorized user request [code = {}]", code);
            redirectAttributes.addFlashAttribute(
                    ERROR_ATTRIBUTE_NAME,
                    bindingErrorResolver.resolveMessage("facebook.error.unauthorizedAssociation", locale)
            );
            LOG.error("Unauthorized Facebook account association");
        } else {
            FBUserDTO fbUserDTO = fbService.associateAccount(authentication.getName(), code);
            LOG.trace(fbUserDTO);
        }

        return socials(authentication, redirectAttributes);
    }

    @RequestMapping(value = FB_CALLBACK_URL, method = RequestMethod.GET, params = {"error", "state=ASSOCIATE"})
    public ModelAndView associateAccountError(@RequestParam("error") String error,
                                              Authentication authentication,
                                              Locale locale,
                                              RedirectAttributes redirectAttributes) {
        LOG.traceEntry("[error = {}]", error);

        ModelAndView modelAndView = socials(authentication, redirectAttributes);
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE_NAME, bindingErrorResolver.resolveMessage("facebook.error.associate", locale));

        return modelAndView;
    }

    @RequestMapping(value = FB_CALLBACK_URL, method = RequestMethod.GET, params = {"code", "state=LOGIN"})
    public ModelAndView authorization(@RequestParam("code") String code,
                                      @RequestParam("state") FBState state,
                                      HttpServletRequest request) {
        LOG.traceEntry("State = {}", state);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));

        UserInfoDTO userInfoDTO = fbService.findUserInfoByCode(code);

        AuthenticationUtils.login(userInfoDTO.getLogin(), userInfoDTO.getRole().toString(), request);
        modelAndView.setView(AuthenticationUtils.userHomeRedirectView(userInfoDTO.getLogin()));

        LOG.trace("Facebook user was authorized as [login = ]", userInfoDTO.getLogin());
        return modelAndView;
    }


    @RequestMapping(value = FB_CALLBACK_URL, method = RequestMethod.GET, params = {"error", "state=LOGIN"})
    public ModelAndView authorizationError(@RequestParam("error") String error,
                                           Locale locale,
                                           RedirectAttributes redirectAttributes) {
        LOG.traceEntry("[error = {}]", error);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, true));
        redirectAttributes.addFlashAttribute(
                ERROR_AUTH_ATTRIBUTE_NAME,
                bindingErrorResolver.resolveMessage("facebook.error.authorization", locale)
        );

        LOG.trace("Facebook user has not been authorized");
        return modelAndView;
    }

    @RequestMapping(value = FB_CALLBACK_URL, method = RequestMethod.GET, params = {"error"})
    public ModelAndView error(@RequestParam("error") String error,
                              Locale locale) {
        LOG.traceEntry("[error = {}]", error);

        ModelAndView modelAndView = new ModelAndView("errors/commonError");
        modelAndView.addObject("status", "Facebook error");
        modelAndView.addObject("error", bindingErrorResolver.resolveMessage("facebook.error.general", locale));
        modelAndView.addObject("trace", "error = " + error);

        return modelAndView;
    }
}
