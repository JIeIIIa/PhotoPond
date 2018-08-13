package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/public/user/{userLogin}")
public class UserPublicInformationController {
    private static final Logger LOG = LogManager.getLogger(UserPublicInformationController.class);

    private final UserInfoService userInfoService;

    @Autowired
    public UserPublicInformationController(UserInfoService userInfoService) {
        LOG.debug("Create instance of {}", UserPublicInformationController.class.getName());
        this.userInfoService = userInfoService;
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> retrieveUserAvatar(@PathVariable("userLogin") String login) {
        LOG.trace("Load avatar for '{}'", login);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userInfoService.retrieveAvatar(login));

    }
}
