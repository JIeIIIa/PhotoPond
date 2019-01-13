package ua.kiev.prog.photopond.drive.exception;

import ua.kiev.prog.photopond.exception.PhotoPondException;

public class DriveException extends PhotoPondException {

    private static final long serialVersionUID = -3170186323514962103L;

    public DriveException() {
    }

    public DriveException(String message) {
        super(message);
    }

    public DriveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DriveException(Throwable cause) {
        super(cause);
    }

    public DriveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
