package ua.kiev.prog.photopond.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@ControllerAdvice
public class MainExceptionController {
    private static Logger log = LogManager.getLogger(MainExceptionController.class);

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String directoryAccessDenied(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String, String> attributes = (Map<String, String>) session.getAttribute("attributeMap");

        String loginUser = attributes.get("userLogin");
        String url = attributes.get("url");
        log.warn("directoryAccessDenied ->   url: " + url + "      access denied for userLogin = '" + loginUser + "'");
        return "Happy access denied!   userLogin = " + loginUser + "   url: " + url;
    }
}
