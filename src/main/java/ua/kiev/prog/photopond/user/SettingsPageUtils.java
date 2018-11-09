package ua.kiev.prog.photopond.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import static java.util.Objects.isNull;

public class SettingsPageUtils {

    private static final Integer SETTINGS_SOCIALS_ITEM_INDEX = 3;

    public static String url(Authentication authentication) {
        if (isNull(authentication) || !authentication.isAuthenticated()) {
            return "";
        }
        return "/user/" + authentication.getName() + "/settings";
    }

    public static ModelAndView socials(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (isNull(authentication) || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Can not build settings page url for unauthenticated user");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView(new RedirectView(url(authentication), true, true, true));
        redirectAttributes.addFlashAttribute("startItem", SETTINGS_SOCIALS_ITEM_INDEX);

        return modelAndView;
    }
}
