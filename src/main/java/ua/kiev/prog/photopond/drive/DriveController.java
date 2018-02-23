package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ua.kiev.prog.photopond.drive.directories.Directory;
import ua.kiev.prog.photopond.user.UserInfo;
import ua.kiev.prog.photopond.user.UserInfoService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.security.Principal;

@Controller
@RequestMapping(value = "/drive")
public class DriveController {
    private static final Logger log = LogManager.getLogger(DriveController.class);

    private final DriveService driveService;

    private final UserInfoService userService;

    @Autowired
    public DriveController(DriveService driveService, UserInfoService userService) {
        this.driveService = driveService;
        this.userService = userService;
    }

    @RequestMapping(value = "/{userLogin}/**", method = RequestMethod.GET)
    public ModelAndView echo(Principal principal, ModelAndView modelAndView,
                             @PathVariable("userLogin") String userLogin, HttpServletRequest request) throws DriveException {
        String tail = request.getRequestURI();
        tail = tail.replaceFirst("/drive/" + userLogin, "");
        String path = userLogin + tail;
        if (tail.isEmpty()) {
            tail = Directory.SEPARATOR;
        }
        UserInfo user = userService.getUserByLogin(principal.getName());
        Content content = driveService.getDirectoryContent(user, tail);

        modelAndView.setViewName("/drive/directory");
        modelAndView.addObject("content", content);

        log.debug("echo:   " + tail);
        return modelAndView;
    }

    @RequestMapping(value = "{userLogin}/**", method = RequestMethod.POST)
    public ModelAndView createDirectory(Principal principal,
                                        @PathVariable("userLogin") String userLogin,
                                        @NotNull @RequestParam("directoryId") Long directoryId,
                                        @NotNull @RequestParam("newDirectoryName") String newDirectoryName,
                                        HttpServletRequest request) throws DriveException {
        String tail = request.getRequestURI();
        tail = tail.replaceFirst("/drive/" + userLogin, "");
        if (tail.isEmpty()) {
            tail = Directory.SEPARATOR;
        }
        UserInfo user = userService.getUserByLogin(principal.getName());
        Directory parentDirectory = driveService.addDirectory(user, directoryId, newDirectoryName);

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/drive{fullPath}")
                .build()
                .expand(parentDirectory.getFullPath())
                .encode();
        RedirectView redirectView = new RedirectView(uriComponents.toUriString(), true, true, false);
        ModelAndView modelAndView = new ModelAndView(redirectView);
        modelAndView.setStatus(HttpStatus.PERMANENT_REDIRECT);

        log.debug("created:   " + tail);
        return modelAndView;
    }
}
