package ua.kiev.prog.photopond.twitter.Exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import static ua.kiev.prog.photopond.twitter.TwitterRequestMappingConstants.ACCOUNT_VIEW_URL;

@ControllerAdvice
public class TwitterControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(TwitterControllerAdvice.class);

    @ExceptionHandler(value = {TwitterAccountAlreadyAssociateException.class})
    public ModelAndView accountAlreadyAssociate(RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Caught exception = {}", TwitterAccountAlreadyAssociateException.class.getName());

        return associatePage("Some account already has been associated", redirectAttributes);
    }

    @ExceptionHandler(value = {AssociateTwitterAccountException.class})
    public ModelAndView associateAccount(RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Caught exception = {}", AssociateTwitterAccountException.class.getName());

        return associatePage("Some error occurs", redirectAttributes);
    }

    @ExceptionHandler(value = {DisassociateTwitterAccountException.class})
    public ModelAndView disassociateException(RedirectAttributes redirectAttributes) {
        LOG.traceEntry("Caught exception = {}", DisassociateTwitterAccountException.class.getName());

        return associatePage("Failure disassociate account", redirectAttributes);
    }

    private ModelAndView associatePage(String error, RedirectAttributes redirectAttributes) {
        LOG.trace("redirect to {}", ACCOUNT_VIEW_URL);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView(ACCOUNT_VIEW_URL, true, true, false));
        redirectAttributes.addFlashAttribute("twitterErrorMessage", error);

        return modelAndView;
    }

    @ExceptionHandler(value = {TwitterAuthenticationException.class})
    public ModelAndView authenticationError(RedirectAttributes redirectAttributes) {
        LOG.trace("redirect to '/login' page");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));
        redirectAttributes.addFlashAttribute("twitterAuthError", "Some authentication error occurred");

        return modelAndView;
    }
}