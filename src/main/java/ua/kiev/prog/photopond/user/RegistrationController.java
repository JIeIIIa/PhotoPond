package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import javax.validation.Valid;


@Controller
public class RegistrationController {
    private static Logger log = LogManager.getLogger(RegistrationController.class);

    @Autowired
    private UserInfoSimpleRepository userInfoSimpleRepository;

/*
    @Autowired
    private UserInfoValidator userInfoValidator;

    @InitBinder("userInfo")
    public void dataBinding(WebDataBinder binder) {
        binder.addValidators(userInfoValidator);
    }
*/

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registrationPage(ModelAndView modelAndView) {
        log.debug("Request to /registration    method=GET");
        modelAndView.setViewName("registration");
        UserInfo user = new UserInfo();
        user.setLogin("qwe");
        modelAndView.addObject("userInfo", user);

        return modelAndView;
    }

    @RequestMapping(value = "/registration",     method = RequestMethod.POST)
    public ModelAndView registrationAddPage(@Valid @ModelAttribute("userInfo") UserInfo userInfo,
                                            BindingResult bindingResult, ModelAndView modelAndView) throws AddToRepositoryException {
        log.debug("Request to /registration    method=POST");
        if (bindingResult.hasErrors()) {
            log.debug("   has error redirect to registration");

            modelAndView.setViewName("registration");
        } else {
            log.debug("   no error redirect to Success");
            userInfo.setRole(UserRole.USER);
            userInfoSimpleRepository.addUser(userInfo);
            modelAndView.setViewName("Success");
        }
        return modelAndView;
    }
}
