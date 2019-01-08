package ua.kiev.prog.photopond.drive.exception;

public class DirectoryModificationException extends DirectoryException {

    private static final long serialVersionUID = -3052114683378351047L;

    public DirectoryModificationException() {
    }

    public DirectoryModificationException(String message) {
        super(message);
    }

    public DirectoryModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectoryModificationException(Throwable cause) {
        super(cause);
    }

    public DirectoryModificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
