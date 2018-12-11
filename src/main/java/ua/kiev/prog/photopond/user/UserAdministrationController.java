package ua.kiev.prog.photopond.user;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ua.kiev.prog.photopond.transfer.AdminEditing;

import java.util.List;

import static ua.kiev.prog.photopond.Utils.Utils.jsonHeader;

@Controller
@RequestMapping("/administration")
public class UserAdministrationController {
    private static final Logger LOG = LogManager.getLogger(UserAdministrationController.class);

    private final UserInfoService userInfoService;

    @Autowired
    public UserAdministrationController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<List<UserInfoDTO>> retrieveAllUsers() {
        LOG.traceEntry();
        List<UserInfoDTO> users = userInfoService.findAllUsers();

        return new ResponseEntity<>(users, jsonHeader(), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<UserInfoDTO> deleteUser(@PathVariable("id") long id) {
        LOG.trace("Delete user by [id = " + id + "]");

        return userInfoService.delete(id)
                .map(u -> {
                    LOG.debug("User with [id = " + id + "] was deleted");
                    return new ResponseEntity<>(u, jsonHeader(), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    LOG.debug("Error: User with [id = " + id + "] not deleted");
                    return new ResponseEntity<>(jsonHeader(), HttpStatus.NO_CONTENT);
                });
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<UserInfoDTO> updateUser(@PathVariable("id") long id,
                                                  @Validated(AdminEditing.class) @RequestBody UserInfoDTO userInfoDTO,
                                                  BindingResult bindingResult) {
        LOG.trace("Update user information for: " + userInfoDTO);
        if (bindingResult.getErrorCount() >= 2 || (bindingResult.hasFieldErrors("password") && userInfoDTO.getPassword() != null)) {
            LOG.debug("Error: User with [id = " + id + "] not modified");
            return new ResponseEntity<>(jsonHeader(), HttpStatus.NO_CONTENT);
        }
        userInfoDTO.setId(id);
        return userInfoService.updateBaseInformation(userInfoDTO)
                .map(u -> {
                    LOG.debug("User with [id = " + id + "] was modified");
                    return new ResponseEntity<>(u, jsonHeader(), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    LOG.debug("Error: User with [id = " + id + "] not modified");
                    return new ResponseEntity<>(jsonHeader(), HttpStatus.NO_CONTENT);
                });
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    @JsonView(value = {AdminEditing.class})
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable("id") long id) {
        LOG.trace("Get user by [id = " + id + "]");

        return userInfoService.findById(id)
                .map(u -> {
                    LOG.debug("User with [id = " + id + "] was found");
                    return new ResponseEntity<>(u, jsonHeader(), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    LOG.debug("User with [id = " + id + "] not found");
                    return new ResponseEntity<>(jsonHeader(), HttpStatus.NO_CONTENT);
                });
    }
}
