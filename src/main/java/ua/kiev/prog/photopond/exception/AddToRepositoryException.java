package ua.kiev.prog.photopond.exception;

public class AddToRepositoryException extends RepositoryException {

    private static final long serialVersionUID = 6699665700966981264L;

    public AddToRepositoryException() {
    }

    public AddToRepositoryException(String message) {
        super(message);
    }

    public AddToRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public AddToRepositoryException(Throwable cause) {
        super(cause);
    }

    public AddToRepositoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
