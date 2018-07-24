package ua.kiev.prog.photopond.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import ua.kiev.prog.photopond.drive.DriveException;

import javax.servlet.http.HttpServletRequest;

import static ua.kiev.prog.photopond.Utils.Utils.customPageNotFound;

@ControllerAdvice
public class MainExceptionController {
    private static final Logger LOG = LogManager.getLogger(MainExceptionController.class);

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ModelAndView directoryAccessDenied(HttpServletRequest request) {
        String login = (String) request.getAttribute("userLogin");
        String url = (String) request.getAttribute("url");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errors/accessDenied");
        modelAndView.addObject("url", url);
        modelAndView.addObject("login", login);

        LOG.debug("Access denied for user '{}':   url = {}", login, url);
        return modelAndView;
    }

    @ExceptionHandler(DriveException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void someDriveExceptionOccur(DriveException e) {
        LOG.debug(e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView pageNotFound(HttpServletRequest request) {
        LOG.debug("url = {}", request.getRequestURL());

        return customPageNotFound(request.getRequestURL().toString());
    }
}
