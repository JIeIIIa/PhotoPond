package ua.kiev.prog.photopond.drive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.kiev.prog.photopond.drive.exception.DriveException;

import javax.servlet.http.HttpServletRequest;

import static ua.kiev.prog.photopond.Utils.Utils.getUriTail;
import static ua.kiev.prog.photopond.drive.directories.Directory.buildPath;

@Controller
@RequestMapping("/user/{login}/files")
public class FileController {

    private final DriveService driveService;

    @Autowired
    public FileController(DriveService driveService) {
        this.driveService = driveService;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPicture(@PathVariable("login") String ownerLogin,
                                             HttpServletRequest request) {
        byte[] data;
        try {
            data = driveService.retrievePictureFileData(ownerLogin, getUriTail(request, ownerLogin));
        } catch (DriveException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(data);
    }

    @RequestMapping(value = "/**", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteFile(@PathVariable("login") String ownerLogin,
                                     HttpServletRequest request) throws DriveException {
        try {
            driveService.deletePictureFile(ownerLogin, getUriTail(request, ownerLogin));
        } catch (DriveException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/**", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity renameFile(@PathVariable("login") String ownerLogin,
                                     @RequestBody DriveItemDTO elementDTO,
                                     HttpServletRequest request) {
        if (!DriveItemType.FILE.equals(elementDTO.getType())) {
//            LOG.trace("Bad request parameter: expected type is FILE");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        try {
            String oldPath = getUriTail(request, ownerLogin);
            String newPath = getUriTail(buildPath(elementDTO.getParentUri(), elementDTO.getName()), ownerLogin);
            driveService.movePictureFile(ownerLogin, oldPath, newPath);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }


}
