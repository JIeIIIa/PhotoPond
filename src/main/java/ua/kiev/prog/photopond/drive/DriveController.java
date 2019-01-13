package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ua.kiev.prog.photopond.drive.exception.DriveException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static ua.kiev.prog.photopond.Utils.Utils.getUriTail;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@Controller
@RequestMapping(value = "/user/{login}/drive")
public class DriveController {
    private static final Logger LOG = LogManager.getLogger(DriveController.class);

    private final DriveService driveService;

    @Autowired
    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ModelAndView directoryPage(ModelAndView modelAndView,
                                      @PathVariable("login") String userLogin,
                                      HttpServletRequest request) throws DriveException {
        String tail = getUriTail(request, userLogin);

        modelAndView.setViewName("drive/directory");

        LOG.debug("echo:   " + tail);
        return modelAndView;
    }

    @RequestMapping(value = "/**", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity move(@PathVariable("login") String userLogin,
                               @NotNull @RequestBody DriveItemDTO elementDTO,
                               HttpServletRequest request) throws DriveException {
        if (!DriveItemType.DIR.equals(elementDTO.getType())) {
            LOG.trace("Bad request parameter: expected type is DIR");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        String targetParentPath = getUriTail(elementDTO.getParentUri(), userLogin);
        String sourcePath = getUriTail(request, userLogin);
        driveService.moveDirectory(
                userLogin,
                sourcePath,
                buildPath(targetParentPath, elementDTO.getName())
        );

        return ResponseEntity.ok().build();
    }


    @RequestMapping(value = "/**", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteDirectory(@PathVariable("login") String ownerLogin,
                                          HttpServletRequest request) throws DriveException {

        driveService.deleteDirectory(ownerLogin, getUriTail(request, ownerLogin));

        return ResponseEntity.ok().build();
    }
}
