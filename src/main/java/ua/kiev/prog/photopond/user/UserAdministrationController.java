package ua.kiev.prog.photopond.user;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ua.kiev.prog.photopond.transfer.AdminEditing;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/administration")
public class UserAdministrationController {
    private static final Logger LOG = LogManager.getLogger(UserAdministrationController.class);

    private final UserInfoService userInfoService;

    @Autowired
    public UserAdministrationController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ModelAndView viewAllUsers(ModelAndView modelAndView) {
        LOG.traceEntry();
        modelAndView.setViewName("/users/allUsers");

        return modelAndView;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<List<UserInfoDTO>> retrieveAllUsers() {
        LOG.traceEntry();
        List<UserInfoDTO> users = userInfoService.findAllUsers();

        return new ResponseEntity<>(users, getHttpJsonHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<UserInfo> deleteUser(@PathVariable("id") long id) {
        LOG.trace("Delete user by [id = " + id + "]");

        Optional<UserInfo> userInfo = userInfoService.delete(id);

        if (!userInfo.isPresent()) {
            LOG.debug("Error: User with [id = " + id + "] not deleted");
            return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
        }
        LOG.debug("User with [id = " + id + "] was deleted");
        return new ResponseEntity<>(userInfo.get(), getHttpJsonHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<UserInfoDTO> updateUser(@PathVariable("id") long id,
                                                  @Validated(AdminEditing.class) @RequestBody UserInfoDTO userInfoDTO,
                                                  BindingResult bindingResult) {
        LOG.trace("Update user information for: " + userInfoDTO);
        if (bindingResult.getErrorCount() >= 2 || (bindingResult.hasFieldErrors("password") && userInfoDTO.getPassword() != null)) {
            LOG.debug("Error: User with [id = " + id + "] not modified");
            return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
        }
        userInfoDTO.setId(id);
        return userInfoService.updateBaseInformation(userInfoDTO)
                .map(u -> {
                    LOG.debug("User with [id = " + id + "] was modified");
                    return new ResponseEntity<>(u, getHttpJsonHeaders(), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    LOG.debug("Error: User with [id = " + id + "] not modified");
                    return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
                });
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable("id") long id) {
        LOG.trace("Get user by [id = " + id + "]");

        return userInfoService.findById(id)
                .map(u -> {
                    LOG.debug("User with [id = " + id + "] was found");
                    return new ResponseEntity<>(u, getHttpJsonHeaders(), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    LOG.debug("User with [id = " + id + "] not found");
                    return new ResponseEntity<>(getHttpJsonHeaders(), HttpStatus.NO_CONTENT);
                });

    }

    private HttpHeaders getHttpJsonHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return responseHeaders;
    }
}
