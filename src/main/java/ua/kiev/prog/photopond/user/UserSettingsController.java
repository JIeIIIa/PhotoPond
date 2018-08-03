package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.kiev.prog.photopond.core.BindingErrorDTO;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.core.BindingErrorResolverImpl;
import ua.kiev.prog.photopond.transfer.ChangePassword;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/user/{login}/settings")
public class UserSettingsController {

    private static final Logger LOG = LogManager.getLogger(UserSettingsController.class);

    private BindingErrorResolver bindingErrorResolver;

    private UserInfoService userInfoService;

    @Autowired
    public UserSettingsController(BindingErrorResolverImpl bindingErrorResolver,
                                  UserInfoService userInfoService) {
        this.bindingErrorResolver = bindingErrorResolver;
        this.userInfoService = userInfoService;
    }

    @RequestMapping("")
    public String allSettings(@PathVariable("login") String login) {
        LOG.traceEntry("Settings for user = '{}'", login);

        return "/settings/settings";
    }

    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity changePassword(@PathVariable("login") String login,
                                         @Validated(ChangePassword.class) @RequestBody UserInfoDTO userInfoDTO,
                                         BindingResult bindingResult,
                                         Locale locale) {
        userInfoDTO.setLogin(login);
        if (bindingResult.hasErrors()) {
            List<BindingErrorDTO> errors = bindingErrorResolver.resolveAll(bindingResult.getAllErrors(), locale);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .headers(jsonHeader())
                    .body(errors);
        }

        if (userInfoService.setNewPassword(userInfoDTO)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(textHeader())
                    .body(bindingErrorResolver.resolveMessage("Success.user.password.change", null, locale));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .headers(textHeader())
                    .body(bindingErrorResolver.resolveMessage("Errors.user.password.change", null, locale));
        }
    }

    private HttpHeaders jsonHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        return headers;
    }

    private HttpHeaders textHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        return headers;
    }
}
