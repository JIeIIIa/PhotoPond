package ua.kiev.prog.photopond.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import java.util.Map;
import java.util.Optional;

import static ua.kiev.prog.photopond.Utils.Utils.customPageNotFound;

@Controller
public class CustomErrorController implements ErrorController {
    private static final Logger LOG = LogManager.getLogger(CustomErrorController.class);

    private static final String ERROR_PATH = "error";

    @Value("${includeStackTrace}")
    private boolean includeStackTrace;

    private final ErrorAttributes errorAttributes;

    @Autowired
    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = ERROR_PATH)
    public ModelAndView error(WebRequest request, ModelAndView modelAndView) {

        Map<String, Object> errorAttributes = getErrorAttributes(request, includeStackTrace);
        if (includeStackTrace) {
            LOG.debug(errorAttributes);
        } else {
            LOG.debug(errorAttributes.get("message"));
        }

        modelAndView.addAllObjects(errorAttributes);
        String url = Optional.ofNullable(
                (String) (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI, 0)))
                .orElse("");
        modelAndView.addObject("url", url);
        modelAndView.setViewName("errors/commonError");
        return modelAndView;
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    private Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
        return errorAttributes.getErrorAttributes(request, includeStackTrace);
    }


    @RequestMapping("/test/NullPointerException")
    public void NPE() {
        throw new NullPointerException("from CustomErrorController");
    }

    @RequestMapping(value = "/page-not-found", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView pageNotFound(@RequestParam(name = "url", required = false) String url) {
        LOG.debug("url = {}", url);

        return customPageNotFound(url);
    }
}
