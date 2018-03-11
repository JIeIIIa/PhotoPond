package ua.kiev.prog.photopond.drive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ua.kiev.prog.photopond.drive.pictures.PictureFile;
import ua.kiev.prog.photopond.drive.pictures.PictureFileDTO;
import ua.kiev.prog.photopond.user.UserInfoService;

import javax.servlet.http.HttpServletRequest;

import static ua.kiev.prog.photopond.Utils.Utils.getUriTail;

@Controller
@RequestMapping("/user/{login}/files")
public class FileController {

    final UserInfoService userInfoService;
    final DriveService driveService;

    @Autowired
    public FileController(UserInfoService userInfoService, DriveService driveService) {
        this.userInfoService = userInfoService;
        this.driveService = driveService;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPicture(@PathVariable("login") String ownerLogin,
                                             HttpServletRequest request) {
        byte[] data;
        try {
            data = driveService.getFile(ownerLogin, getUriTail(request, ownerLogin));
        } catch (DriveException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(data);
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ModelAndView addFile(@PathVariable("login") String ownerLogin,
                                @RequestParam("files") MultipartFile[] files,
                                HttpServletRequest request) throws DriveException {
        PictureFile lastCreatedFile = null;

        try {
            for (MultipartFile file : files) {
                lastCreatedFile = driveService.addPictureFile(ownerLogin, getUriTail(request, ownerLogin), file);
            }
        } catch (DriveException e) {
            throw new DriveException("Cannot create file in directory = " + getUriTail(request, ownerLogin));
        }
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/user/{login}/drive{fullPath}")
                .build()
                .expand(ownerLogin, lastCreatedFile.getDirectory().getPath())
                .encode();
        RedirectView redirectView = new RedirectView(uriComponents.toUriString(), true, true, false);
        ModelAndView modelAndView = new ModelAndView(redirectView);
        modelAndView.setStatus(HttpStatus.PERMANENT_REDIRECT);

        return modelAndView;
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
                                     @RequestBody PictureFileDTO fileDTO,
                                     HttpServletRequest request) {
        try {
            driveService.movePictureFile(ownerLogin, getUriTail(request, ownerLogin), getUriTail(fileDTO.getPath(), ownerLogin));
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok().build();
    }


}
