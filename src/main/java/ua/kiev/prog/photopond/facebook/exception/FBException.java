package ua.kiev.prog.photopond.facebook.exception;


import ua.kiev.prog.photopond.exception.PhotoPondException;

public class FBException extends PhotoPondException {

    private static final long serialVersionUID = 5950588068795379030L;

    public FBException() {
    }

    public FBException(String message) {
        super(message);
    }

    public FBException(String message, Throwable cause) {
        super(message, cause);
    }

    public FBException(Throwable cause) {
        super(cause);
    }

    public FBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
