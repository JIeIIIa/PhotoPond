package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.core.BindingErrorResolverImpl;
import ua.kiev.prog.photopond.transfer.ChangeAvatar;
import ua.kiev.prog.photopond.transfer.ChangePassword;

import java.util.Locale;

import static ua.kiev.prog.photopond.Utils.Utils.jsonHeader;
import static ua.kiev.prog.photopond.Utils.Utils.textHeader;

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
                                         Locale locale) {
        userInfoDTO.setLogin(login);

        if (userInfoService.setNewPassword(userInfoDTO)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(textHeader())
                    .body(bindingErrorResolver.resolveMessage("user.password.change.success", null, locale));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .headers(textHeader())
                    .body(bindingErrorResolver.resolveMessage("user.password.change.error", null, locale));
        }
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity updateAvatar(@PathVariable("login") String login,
                                       @Validated(ChangeAvatar.class) @ModelAttribute UserInfoDTO userInfoDTO,
                                       Locale locale) {
        userInfoDTO.setLogin(login);

        if (userInfoService.updateAvatar(userInfoDTO)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(textHeader())
                    .body(bindingErrorResolver.resolveMessage("user.avatar.change.success", null, locale));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .headers(textHeader())
                    .body(bindingErrorResolver.resolveMessage("user.avatar.change.error", null, locale));
        }
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.DELETE)
    public ResponseEntity removeAvatar(@PathVariable("login") String login,
                                       Locale locale) {
        if (userInfoService.updateAvatar(
                UserInfoDTOBuilder.getInstance().login(login).build()
        )) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(jsonHeader())
                    .body(bindingErrorResolver.resolveMessage("user.avatar.remove.success", null, locale));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .headers(jsonHeader())
                    .body(bindingErrorResolver.resolveMessage("user.avatar.change.error", null, locale));
        }
    }
}
