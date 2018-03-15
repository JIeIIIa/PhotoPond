package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.drive.directories.DirectoryDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static ua.kiev.prog.photopond.Utils.Utils.getUriTail;

@Controller
@RequestMapping(value = "/user/{login}/drive")
public class DriveController {
    private static final Logger log = LogManager.getLogger(DriveController.class);

    private final DriveService driveService;

    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ModelAndView getDirectory(ModelAndView modelAndView,
                             @PathVariable("login") String userLogin,
                             HttpServletRequest request) throws DriveException {
        String tail = getUriTail(request, userLogin);

        Content content = driveService.getDirectoryContent(userLogin, tail);

        modelAndView.setViewName("/drive/directory");
        modelAndView.addObject("content", content);

        log.debug("echo:   " + tail);
        return modelAndView;
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ModelAndView createDirectory(
                                        @PathVariable("login") String userLogin,
                                        @NotNull @RequestParam("newDirectoryName") String newDirectoryName,
                                        HttpServletRequest request) throws DriveException {

        Directory createdDirectory = driveService.addDirectory(userLogin, getUriTail(request, userLogin), newDirectoryName);

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/user/{login}/drive{path}")
                .build()
                .expand(userLogin, createdDirectory.getParentPath())
                .encode();
        RedirectView redirectView = new RedirectView(uriComponents.toUriString(), true, true, false);
        ModelAndView modelAndView = new ModelAndView(redirectView);
        modelAndView.setStatus(HttpStatus.PERMANENT_REDIRECT);

        log.debug("created:   " + createdDirectory);
        return modelAndView;
    }

    @RequestMapping(value = "/**", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity move(@PathVariable("login") String userLogin,
                                             @NotNull @RequestBody DirectoryDTO targetDirectoryDTO,
                                             HttpServletRequest request) throws DriveException {

        try {
            driveService.moveDirectory(
                    userLogin,
                    getUriTail(request, userLogin),
                    getUriTail(targetDirectoryDTO.getPath(), userLogin)
            );
        } catch (DriveException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().build();
    }
}
