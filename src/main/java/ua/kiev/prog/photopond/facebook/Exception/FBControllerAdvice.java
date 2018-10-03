package ua.kiev.prog.photopond.facebook.Exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

import static ua.kiev.prog.photopond.facebook.FBRequestMappingConstants.ACCOUNT_VIEW_URL;

@ControllerAdvice
public class FBControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(FBControllerAdvice.class);

    @ExceptionHandler(value = {FBAccountAlreadyAssociateException.class})
    public ModelAndView accountAlreadyAssociate(RedirectAttributes redirectAttributes) {
        return associatePage("Some account already has been associated", redirectAttributes);
    }

    @ExceptionHandler(value = {AssociateFBAccountException.class})
    public ModelAndView associateAccount(RedirectAttributes redirectAttributes) {
        return associatePage("Some error occurs", redirectAttributes);
    }

    @ExceptionHandler(value = {DisassociateFBAccountException.class})
    public ModelAndView disassociateException(RedirectAttributes redirectAttributes) {
        return associatePage("Failure disassociate account", redirectAttributes);
    }

    private ModelAndView associatePage(String error, RedirectAttributes redirectAttributes) {
        LOG.trace("redirect to {}", ACCOUNT_VIEW_URL);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView(ACCOUNT_VIEW_URL, true, true, false));
        redirectAttributes.addFlashAttribute("fbErrorMessage", error);

        return modelAndView;
    }

    @ExceptionHandler(value = {FBAuthenticationException.class})
    public ModelAndView authenticationError(RedirectAttributes redirectAttributes,
                                            HttpServletRequest request) {
        LOG.trace("redirect to '/login' page");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView("/login", true, true, false));
        redirectAttributes.addFlashAttribute("fbAuthError", "Some authentication error occurred");

        return modelAndView;
    }
}
