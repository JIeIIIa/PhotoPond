package ua.kiev.prog.photopond.drive.exception;

public class DirectoryException extends DriveException {

    private static final long serialVersionUID = 2983899395480715171L;

    public DirectoryException() {
    }

    public DirectoryException(String message) {
        super(message);
    }

    public DirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectoryException(Throwable cause) {
        super(cause);
    }

    public DirectoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
