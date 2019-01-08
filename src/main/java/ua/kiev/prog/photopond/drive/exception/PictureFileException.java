package ua.kiev.prog.photopond.drive.exception;

public class PictureFileException extends DriveException {

    private static final long serialVersionUID = 3884682575151827374L;

    public PictureFileException() {
    }

    public PictureFileException(String message) {
        super(message);
    }

    public PictureFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public PictureFileException(Throwable cause) {
        super(cause);
    }

    public PictureFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
