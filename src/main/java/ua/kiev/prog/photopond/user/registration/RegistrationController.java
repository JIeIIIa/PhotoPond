package ua.kiev.prog.photopond.user.registration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@Controller
public class RegistrationController {
    static final String REGISTRATION_FORM_NAME = "registrationForm";
    private static Logger log = LogManager.getLogger(RegistrationController.class);

    private final UserInfoService userInfoService;

    private final AuthenticationManager authenticationManager;

    private final Validator validator;

    @Autowired
    public RegistrationController(UserInfoService userInfoService, AuthenticationManager authenticationManager,
                                  @Qualifier("registrationFormValidator") Validator validator) {
        this.userInfoService = userInfoService;
        this.authenticationManager = authenticationManager;
        this.validator = validator;
    }

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
        user.setLogin("newUser");
        user.setPassword("qwerty");
        RegistrationForm form = new RegistrationForm(user);
        form.setPasswordConfirmation(user.getPassword());

        modelAndView.addObject(REGISTRATION_FORM_NAME, form);

        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView registrationNewUser(@Valid @ModelAttribute(REGISTRATION_FORM_NAME) RegistrationForm form,
                                            BindingResult bindingResult, ModelAndView modelAndView,
                                            HttpServletRequest request) throws AddToRepositoryException {
        log.debug("Request to /registration    method=POST");
        if (bindingResult.hasErrors()) {
            log.debug("   has error redirect to registration");
            modelAndView.setViewName("registration");
            return modelAndView;
        }

        log.debug("   no error redirect to user home page");
        UserInfo userInfo = form.getUserInfo();

        userInfo.setRole(UserRole.USER);
        userInfoService.addUser(userInfo);

        modelAndView.setView(new RedirectView("/user/" + userInfo.getLogin() + "/", true, true, false));
        autoLogin(userInfo.getLogin(), userInfo.getPassword(), request);
        log.trace("   new user " + userInfo.getLogin() + " was logged in.");

        return modelAndView;
    }

    private void autoLogin(String login, String password, HttpServletRequest request) {
        log.trace("   try to auto log in for user " + login);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, password);
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }
}
