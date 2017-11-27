package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/administration")
public class UserAdministrationController {
    private static Logger log = LogManager.getLogger(UserAdministrationController.class);

    private final UserInfoService userInfoService;

    @Autowired
    public UserAdministrationController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ModelAndView viewAllUsers(ModelAndView modelAndView) {
        log.trace("Get list of users");
        List<UserInfo> users = userInfoService.getAllUsers();
        log.debug("Add list of users in modelAndView");
        modelAndView.addObject("usersList", users);
        modelAndView.setViewName("/users/allUsers");

        return modelAndView;
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<UserInfo> deleteUser(@PathVariable("id") long id) {
        log.trace("Delete user by [id = " + id + "]");
        UserInfo userInfo = userInfoService.delete(id);

        if (userInfo == null) {
            log.debug("Error: User with [id = " + id + "] not deleted");
            return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
        }
        log.debug("User with [id = " + id + "] was deleted");
        return new ResponseEntity<>(userInfo, getHttpJsonHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST)
    public ResponseEntity<UserInfo> updateUser(@PathVariable("id") long id, @Valid @ModelAttribute UserInfo userInfo) {
        log.trace("Update user information for: " + userInfo);
        userInfo.setId(id);
        userInfo = userInfoService.update(userInfo);

        if (userInfo == null) {
            log.debug("Error: User with [id = " + id + "] not modified");
            return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
        }
        log.debug("User with [id = " + id + "] was modified");
        return new ResponseEntity<>(userInfo, getHttpJsonHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public ResponseEntity<UserInfo> updateUser(@PathVariable("id") long id) {
        log.trace("Get user by [id = " + id + "]");

        UserInfo userInfo = userInfoService.getUserById(id);
        if (userInfo == null) {
            log.debug("User with [id = " + id + "] not found");
            return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
        }

        log.debug("User with [id = " + id + "] was found");
        return new ResponseEntity<>(userInfo, getHttpJsonHeaders(), HttpStatus.OK);
    }

    private HttpHeaders getHttpJsonHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return responseHeaders;
    }
}
