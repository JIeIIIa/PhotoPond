package ua.kiev.prog.photopond.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {
    private static Logger log = LogManager.getLogger(CustomErrorController.class);

    private static final String ERROR_PATH=  "/error";

    @Value("${includeStackTrace}")
    private boolean includeStackTrace;



    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = ERROR_PATH)
    public ModelAndView error(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) {

        Map<String, Object> errorAttributes = getErrorAttributes(request, includeStackTrace);
        if (includeStackTrace) {
            log.debug(errorAttributes);
        } else {
            log.debug(errorAttributes.get("message"));
        }

        modelAndView.addAllObjects(errorAttributes);

        return modelAndView;
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }


    @RequestMapping("/test/NullPointerException")
    public void NPE() {
        throw new NullPointerException("from CustomErrorController");
    }

}
