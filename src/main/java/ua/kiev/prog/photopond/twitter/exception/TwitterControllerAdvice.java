package ua.kiev.prog.photopond.twitter.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.twitter.TwitterRequestMappingConstants;
import ua.kiev.prog.photopond.user.SettingsPageUtils;

import java.util.Locale;

@ControllerAdvice
public class TwitterControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(TwitterControllerAdvice.class);

    private final BindingErrorResolver bindingErrorResolver;

    @Autowired
    public TwitterControllerAdvice(BindingErrorResolver bindingErrorResolver) {
        LOG.info("Create instance of class {}", this.getClass().getName());
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @ExceptionHandler(value = {TwitterAccountAlreadyAssociateException.class})
    public ModelAndView accountAlreadyAssociate(Authentication authentication,
                                                Locale locale,
                                                RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Caught exception = {}", TwitterAccountAlreadyAssociateException.class.getName());

        return associatePage("twitter.error.accountAlreadyAssociate", authentication, locale, redirectAttributes);
    }

    @ExceptionHandler(value = {AssociateTwitterAccountException.class})
    public ModelAndView associateAccount(Authentication authentication,
                                         Locale locale,
                                         RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Caught exception = {}", AssociateTwitterAccountException.class.getName());

        return associatePage("twitter.error.associate", authentication, locale, redirectAttributes);
    }

    @ExceptionHandler(value = {DisassociateTwitterAccountException.class})
    public ModelAndView disassociateException(Authentication authentication,
                                              Locale locale,
                                              RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Caught exception = {}", DisassociateTwitterAccountException.class.getName());

        return associatePage("twitter.error.disassociate", authentication, locale, redirectAttributes);
    }

    private ModelAndView associatePage(String messageKey,
                                       Authentication authentication,
                                       Locale locale,
                                       RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = SettingsPageUtils.socials(authentication, redirectAttributes);
        LOG.trace("redirect to {}", modelAndView.getView());

        String message = bindingErrorResolver.resolveMessage(messageKey, locale);
        redirectAttributes.addFlashAttribute(TwitterRequestMappingConstants.ERROR_ATTRIBUTE_NAME, message);

        return modelAndView;
    }

    @ExceptionHandler(value = {TwitterAuthenticationException.class})
    public ModelAndView authenticationError(Locale locale,
                                            RedirectAttributes redirectAttributes) {
        LOG.trace("redirect to '/login' page");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));
        String message = bindingErrorResolver.resolveMessage("twitter.error.authorization", locale);
        redirectAttributes.addFlashAttribute(TwitterRequestMappingConstants.ERROR_AUTH_ATTRIBUTE_NAME, message);

        return modelAndView;
    }

    @ExceptionHandler(value = {NotFoundTwitterAssociatedAccountException.class})
    public ResponseEntity<String> notFoundAssociatedAccount(Locale locale) {
        String message = bindingErrorResolver.resolveMessage("twitter.error.notFoundAccount", locale);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(message);
    }

    @ExceptionHandler(value = {TweetPublishingException.class})
    public ResponseEntity<String> tweetPublishingFailure(Locale locale) {
        String message = bindingErrorResolver.resolveMessage("twitter.error.publishing", locale);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }
}