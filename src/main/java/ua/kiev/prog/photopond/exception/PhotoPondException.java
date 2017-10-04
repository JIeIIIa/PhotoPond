package ua.kiev.prog.photopond.exception;

public class PhotoPondException extends Exception {
    public PhotoPondException() {
        super();
    }

    public PhotoPondException(String message) {
        super(message);
    }

    public PhotoPondException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhotoPondException(Throwable cause) {
        super(cause);
    }

    protected PhotoPondException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
