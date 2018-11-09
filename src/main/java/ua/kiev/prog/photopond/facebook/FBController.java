package ua.kiev.prog.photopond.facebook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.*;
import static ua.kiev.prog.photopond.user.SettingsPageUtils.socials;

@Controller
public class FBController {

    private static final Logger LOG = LogManager.getLogger(FBController.class);

    private final FBService fbService;

    @Autowired
    public FBController(FBService fbService) {
        LOG.info("Create instance of class {}", this.getClass().getName());
        this.fbService = fbService;
    }

    @RequestMapping(value = ACCOUNT_VIEW_URL)
    public ModelAndView associateList(ModelAndView modelAndView,
                                      Authentication authentication) {
        LOG.traceEntry("Request to {}", ACCOUNT_VIEW_URL);
        modelAndView.setViewName("facebook/associate");
        FBUserDTO fbUserDTO = fbService.findAccountByLogin(authentication.getName());
        modelAndView.addObject("fbUserDTO", fbUserDTO);

        return modelAndView;
    }

    @RequestMapping(value = ACCOUNTS_LIST_URL)
    @ResponseBody
    public ResponseEntity<FBUserDTO> associatedList(Authentication authentication) {
        LOG.trace("Request to {}", ACCOUNTS_LIST_URL);
        FBUserDTO fbUserDTO = fbService.findAccountByLogin(authentication.getName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fbUserDTO);
    }

    @RequestMapping(value = DISASSOCIATE_ACCOUNT_URL, method = RequestMethod.POST)
    public ModelAndView disassociateAccount(Authentication authentication,
                                            RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Request to {}", DISASSOCIATE_ACCOUNT_URL);
        fbService.disassociateAccount(authentication.getName());

        return socials(authentication, redirectAttributes);
    }

    @RequestMapping(value = ASSOCIATE_ACCOUNT_URL, method = RequestMethod.GET)
    public ModelAndView associateRedirect() {
        LOG.traceEntry("{} redirect to {}", ASSOCIATE_ACCOUNT_URL, FBConstants.associateAccountUrl());
        return new ModelAndView(new RedirectView(FBConstants.associateAccountUrl(), true, true));
    }

    @RequestMapping(value = AUTHENTICATION_WITH_FACEBOOK_URL, method = RequestMethod.GET)
    public ModelAndView authorizationRedirect() {
        LOG.traceEntry("{} redirect to {}", AUTHENTICATION_WITH_FACEBOOK_URL, FBConstants.facebookLoginUrl());
        return new ModelAndView(new RedirectView(FBConstants.facebookLoginUrl(), true, true));
    }
}
