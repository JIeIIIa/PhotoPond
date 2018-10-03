package ua.kiev.prog.photopond.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import ua.kiev.prog.photopond.core.BindingErrorDTO;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.drive.DriveException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

import static ua.kiev.prog.photopond.Utils.Utils.customPageNotFound;
import static ua.kiev.prog.photopond.Utils.Utils.jsonHeader;

@ControllerAdvice
public class MainControllerAdvice {
    private static final Logger LOG = LogManager.getLogger(MainControllerAdvice.class);

    private final BindingErrorResolver bindingErrorResolver;

    @Autowired
    public MainControllerAdvice(BindingErrorResolver bindingErrorResolver) {
        this.bindingErrorResolver = bindingErrorResolver;
    }

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

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity bindErrors(MethodArgumentNotValidException ex,
                                     WebRequest request) {
        LOG.traceEntry("Caught MethodArgumentNotValidException: {}", ex.getMessage());
        BindingResult bindingResult = ex.getBindingResult();
        Locale locale = request.getLocale();

        List<BindingErrorDTO> errors = bindingErrorResolver.resolveAll(bindingResult.getAllErrors(), locale);
        LOG.debug(errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .headers(jsonHeader())
                .body(errors);
    }

    @ExceptionHandler(value = {BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity bindErrors_avatar(BindingResult bindingResult,
                                            Locale locale) {
        LOG.traceEntry("Caught BindException");
        List<BindingErrorDTO> errors = bindingErrorResolver.resolveAll(bindingResult.getAllErrors(), locale);
        LOG.debug(errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .headers(jsonHeader())
                .body(errors);
    }
}
