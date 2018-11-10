package ua.kiev.prog.photopond.facebook.Exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.user.SettingsPageUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ERROR_ATTRIBUTE_NAME;
import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ERROR_AUTH_ATTRIBUTE_NAME;

@ControllerAdvice
public class FBControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(FBControllerAdvice.class);

    private final BindingErrorResolver bindingErrorResolver;

    @Autowired
    public FBControllerAdvice(BindingErrorResolver bindingErrorResolver) {
        LOG.info("Create instance of class {}", this.getClass().getName());
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @ExceptionHandler(value = {FBAccountAlreadyAssociateException.class})
    public ModelAndView accountAlreadyAssociate(Authentication authentication,
                                                Locale locale,
                                                RedirectAttributes redirectAttributes) {

        return associatePage("facebook.error.accountAlreadyAssociate", authentication, locale, redirectAttributes);
    }

    @ExceptionHandler(value = {AssociateFBAccountException.class})
    public ModelAndView associateAccount(Authentication authentication,
                                         Locale locale,
                                         RedirectAttributes redirectAttributes) {
        return associatePage("facebook.error.associate", authentication, locale, redirectAttributes);
    }

    @ExceptionHandler(value = {DisassociateFBAccountException.class})
    public ModelAndView disassociateException(Authentication authentication,
                                              Locale locale,
                                              RedirectAttributes redirectAttributes) {
        return associatePage("facebook.error.disassociate", authentication, locale, redirectAttributes);
    }

    private ModelAndView associatePage(String messageKey,
                                       Authentication authentication,
                                       Locale locale,
                                       RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = SettingsPageUtils.socials(authentication, redirectAttributes);
        LOG.trace("redirect to {}", modelAndView.getView());
        String message = bindingErrorResolver.resolveMessage(messageKey, locale);
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE_NAME, message);

        return modelAndView;
    }

    @ExceptionHandler(value = {FBAuthenticationException.class})
    public ModelAndView authenticationError(Locale locale,
                                            RedirectAttributes redirectAttributes,
                                            HttpServletRequest request) {
        LOG.trace("redirect to '/login' page");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));
        String message = bindingErrorResolver.resolveMessage("facebook.error.authorization", locale);
        redirectAttributes.addFlashAttribute(ERROR_AUTH_ATTRIBUTE_NAME, message);

        return modelAndView;
    }
}
