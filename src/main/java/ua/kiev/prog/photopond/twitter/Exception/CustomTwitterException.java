package ua.kiev.prog.photopond.twitter.Exception;

import ua.kiev.prog.photopond.exception.PhotoPondException;

public class CustomTwitterException extends PhotoPondException {

    private static final long serialVersionUID = 1524497700900961844L;

    public CustomTwitterException() {
    }

    public CustomTwitterException(String message) {
        super(message);
    }

    public CustomTwitterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomTwitterException(Throwable cause) {
        super(cause);
    }

    public CustomTwitterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
