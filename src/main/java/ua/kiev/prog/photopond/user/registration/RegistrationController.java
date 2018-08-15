package ua.kiev.prog.photopond.user.registration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;
import ua.kiev.prog.photopond.transfer.New;
import ua.kiev.prog.photopond.user.UserInfoDTO;
import ua.kiev.prog.photopond.user.UserInfoDTOBuilder;
import ua.kiev.prog.photopond.user.UserInfoService;
import ua.kiev.prog.photopond.user.UserRole;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;


@Controller
public class RegistrationController {
    private static final Logger LOG = LogManager.getLogger(RegistrationController.class);

    static final String REGISTRATION_USER_ATTRIBUTE_NAME = "userDTO";

    private final UserInfoService userInfoService;

    @Autowired
    public RegistrationController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registrationPage() {
        LOG.traceEntry("Method = GET,   uri = '/registration'");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("registration");

        UserInfoDTO user = UserInfoDTOBuilder.getInstance()
                .login("newUser")
                .password("qwerty")
                .passwordConfirmation("qwerty")
                .build();
        modelAndView.addObject(REGISTRATION_USER_ATTRIBUTE_NAME, user);
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView registrationNewUser(@Validated(New.class) @ModelAttribute(REGISTRATION_USER_ATTRIBUTE_NAME) UserInfoDTO userDTO,
                                            BindingResult bindingResult, ModelAndView modelAndView,
                                            HttpServletRequest request) throws AddToRepositoryException {
        LOG.traceEntry("Method = POST,   uri = '/registration'");
        if (bindingResult.hasErrors()) {
            LOG.debug("Has errors in registration information. Redirect to /registration");
            modelAndView.setViewName("registration");
            modelAndView.addObject(REGISTRATION_USER_ATTRIBUTE_NAME, userDTO);

            return modelAndView;
        }

        LOG.trace("No error. Save user.");

        userDTO.setRole(UserRole.USER);
        userInfoService.addUser(userDTO);

        LOG.debug("Redirect to user home page.");
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/user/{login}/drive")
                .build()
                .expand(userDTO.getLogin())
                .encode();
        modelAndView.setView(new RedirectView(uriComponents.toUriString(), true, true, false));

        autoLogin(userDTO.getLogin(), request);

        return modelAndView;
    }

    private void autoLogin(String login, HttpServletRequest request) {
        LOG.traceEntry("Try to auto log in for user [ login = '{}' ]", login);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, null,
                singletonList(new SimpleGrantedAuthority(UserRole.USER.toString())));
        SecurityContextHolder.getContext().setAuthentication(token);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        LOG.debug("Exit   New user [ login = '{}' ] was logged in.", login);
    }
}
