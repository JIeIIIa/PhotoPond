package ua.kiev.prog.photopond.Administration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/administration")
public class AdministrationController {
    private static final Logger LOG = LogManager.getLogger(AdministrationController.class);

    @RequestMapping(value = "/adminPanel", method = RequestMethod.GET)
    public ModelAndView viewAllUsers(ModelAndView modelAndView) {
        LOG.traceEntry();
        modelAndView.setViewName("administration/administration");

        return modelAndView;
    }
}
