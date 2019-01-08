package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.kiev.prog.photopond.drive.exception.DriveException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

import static ua.kiev.prog.photopond.Utils.Utils.getUriTail;

@RestController
@RequestMapping(value = "/api/{login}")
public class DriveApiController {
    private static final Logger LOG = LogManager.getLogger(DriveApiController.class);

    private final DriveService driveService;

    @Autowired
    public DriveApiController(DriveService driveService) {
        this.driveService = driveService;
    }

    @RequestMapping(value = "/directories/**", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DirectoriesDTO> retrieveChildDirectories(@PathVariable("login") String userLogin,
                                                                   HttpServletRequest request) throws DriveException {
        String sourcePath = getUriTail(request, userLogin);
        LOG.trace("login = {},   sourcePath = '{}'", userLogin, sourcePath);

        DirectoriesDTO directoriesDTO = driveService.retrieveDirectories(userLogin, sourcePath);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(directoriesDTO);
    }

    @RequestMapping(value = "/directory/**", method = RequestMethod.GET)
    public ResponseEntity<List<DriveItemDTO>> retrieveContent(@PathVariable("login") String userLogin,
                                                              HttpServletRequest request) {
        String sourcePath = getUriTail(request, userLogin);
        LOG.trace("login = {},   sourcePath = '{}'", userLogin, sourcePath);

        List<DriveItemDTO> items = driveService.retrieveContent(userLogin, sourcePath, true);
        LOG.trace(items);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(items);

    }

    @RequestMapping(value = "/directory/**", method = RequestMethod.POST)
    public ResponseEntity<DriveItemDTO> createDirectory(
            @PathVariable("login") String userLogin,
            @NotNull @RequestBody String newDirectoryName,
            HttpServletRequest request) throws DriveException {

        String sourcePath = getUriTail(request, userLogin);
        LOG.trace("login = '{}',    sourcePath = '{}',   newDirectoryName = '{}'", userLogin, sourcePath, newDirectoryName);

        DriveItemDTO createdDirectoryDTO = driveService.addDirectory(userLogin, sourcePath, newDirectoryName);
        LOG.debug("Directory was create:   " + createdDirectoryDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdDirectoryDTO);
    }

    @RequestMapping(value = "/files/**", method = RequestMethod.POST)
    public ResponseEntity addFiles(@PathVariable("login") String ownerLogin,
                                   @RequestParam("files") MultipartFile[] files,
                                   HttpServletRequest request) throws DriveException {
        LinkedList<DriveItemDTO> list = new LinkedList<>();
        String targetDirectoryPath = getUriTail(request, ownerLogin);
        LOG.trace("login = {},   targetDirectoryPath = '{}'", ownerLogin, targetDirectoryPath);

        for (MultipartFile file : files) {
            try {
                DriveItemDTO createdFile = driveService.addPictureFile(ownerLogin, targetDirectoryPath, file);
                list.add(createdFile);
            } catch (DriveException e) {
                LOG.debug("Error in creating file (user = '{}'):   {}/{}", ownerLogin, targetDirectoryPath, file.getOriginalFilename());
            }
        }

        LOG.trace("Created files:  {}", list);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(list);
    }
}
