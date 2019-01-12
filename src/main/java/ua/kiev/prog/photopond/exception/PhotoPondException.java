package ua.kiev.prog.photopond.exception;

public class PhotoPondException extends RuntimeException {

    private static final long serialVersionUID = -3268590186901253996L;

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

    public PhotoPondException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
