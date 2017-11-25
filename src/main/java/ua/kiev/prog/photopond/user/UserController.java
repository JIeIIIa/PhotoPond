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
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/administration")
public class UserController {
    private static Logger log = LogManager.getLogger(UserController.class);

    private final UserInfoService userInfoService;

    @Autowired
    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ModelAndView viewAllUsers(ModelAndView modelAndView) {
        log.trace("viewAllUsers ->  ");
        List<UserInfo> users = userInfoService.getAllUsers();
        modelAndView.addObject("usersList", users);
        modelAndView.setViewName("users/allUsers");

        return modelAndView;
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<UserInfo> deleteUser(@PathVariable("id") long id) {
        log.trace("deleteUser ->  delete user [id = " + id + "]");
        UserInfo userInfo = userInfoService.delete(id);
        if (userInfo == null) {
            log.debug("deleteUser ->  user with id = " + id + " not found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.debug("deleteUser ->  user with id = " + id + " was deleted");
        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }
}
