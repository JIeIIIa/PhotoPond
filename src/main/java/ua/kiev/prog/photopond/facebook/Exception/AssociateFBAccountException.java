package ua.kiev.prog.photopond.facebook.Exception;

public class AssociateFBAccountException extends FBException {

    private static final long serialVersionUID = 4738345322790795079L;

    public AssociateFBAccountException() {
    }

    public AssociateFBAccountException(String message) {
        super(message);
    }

    public AssociateFBAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssociateFBAccountException(Throwable cause) {
        super(cause);
    }

    public AssociateFBAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
