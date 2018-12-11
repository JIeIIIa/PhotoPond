package ua.kiev.prog.photopond.drive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static ua.kiev.prog.photopond.Utils.Utils.jsonHeader;

@Controller
@RequestMapping("/administration")
public class DriveAdministrationController {
    private static final Logger LOG = LogManager.getLogger(DriveAdministrationController.class);

    private final DriveService driveService;

    @Autowired
    public DriveAdministrationController(DriveService driveService) {
        LOG.info("Create instance of {}", DriveAdministrationController.class);
        this.driveService = driveService;
    }

    @RequestMapping("/drive/statistics")
    public ResponseEntity<List<DriveStatisticsDTO>> fullStatistics() {
        LOG.traceEntry();

        List<DriveStatisticsDTO> statistics = driveService.fullStatistics();

        return new ResponseEntity<>(statistics, jsonHeader(), HttpStatus.OK);
    }
}
