package ua.kiev.prog.photopond.user.registration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoSimpleRepository;
import ua.kiev.prog.photopond.user.UserRole;

import javax.validation.Valid;


@Controller
public class RegistrationController {
    public static final String REGISTRATION_FORM_NAME = "form";
    private static Logger log = LogManager.getLogger(RegistrationController.class);

    @Autowired
    private UserInfoSimpleRepository userInfoSimpleRepository;

    @Autowired
    @Qualifier("registrationFormValidator")
    private Validator validator;

    @InitBinder(REGISTRATION_FORM_NAME)
    private void initBinder(WebDataBinder binder) {
        binder.addValidators(validator);
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registrationPage() {
        ModelAndView modelAndView = new ModelAndView();
        log.debug("Request to /registration    method=GET");
        modelAndView.setViewName("registration");
        UserInfo user = new UserInfo();
        user.setLogin("qwe");
        RegistrationForm form = new RegistrationForm(user);
        modelAndView.addObject(REGISTRATION_FORM_NAME, form);

        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView registrationNewUser(@Valid @ModelAttribute(REGISTRATION_FORM_NAME) RegistrationForm form,
                                            BindingResult bindingResult, ModelAndView modelAndView) throws AddToRepositoryException {
        log.debug("Request to /registration    method=POST");
        if (bindingResult.hasErrors()) {
            log.debug("   has error redirect to registration");
            modelAndView.setViewName("registration");
            return modelAndView;
        }

        log.debug("   no error redirect to Success");
        form.getUserInfo().setRole(UserRole.USER);
        userInfoSimpleRepository.addUser(form.getUserInfo());
        modelAndView.setViewName("Success");

        return modelAndView;
    }
}
