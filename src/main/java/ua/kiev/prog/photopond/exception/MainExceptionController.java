package ua.kiev.prog.photopond.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class MainExceptionController {
    private static Logger log = LogManager.getLogger(MainExceptionController.class);

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String directoryAccessDenied(HttpServletRequest request) {
        String loginUser = (String) request.getAttribute("userLogin");
        String url = (String) request.getAttribute("url");

        StringBuilder stringBuilder = new StringBuilder()
                .append("URL: " ).append(url)
                .append("      access denied for [ userLogin = '").append(loginUser).append("' ]");

        log.warn(stringBuilder.toString());
        return "Happy access denied!   " + stringBuilder.toString();
    }
}
