package ua.kiev.prog.photopond.drive.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ua.kiev.prog.photopond.core.BindingErrorResolver;

@ControllerAdvice
public class DriveControllerAdvice {

    private static final Logger LOG = LogManager.getLogger(DriveControllerAdvice.class);

    private final BindingErrorResolver bindingErrorResolver;

    @Autowired
    public DriveControllerAdvice(BindingErrorResolver bindingErrorResolver) {
        LOG.info("Create instance of class {}", this.getClass().getName());
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @ExceptionHandler(DriveException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void someDriveExceptionOccur(DriveException e) {
        LOG.debug(e.getMessage());
    }

}
