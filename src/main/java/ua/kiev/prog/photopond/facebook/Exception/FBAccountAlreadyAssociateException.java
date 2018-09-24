package ua.kiev.prog.photopond.facebook.Exception;

public class FBAccountAlreadyAssociateException extends AssociateFBAccountException {

    private static final long serialVersionUID = -4290191806187510442L;

    public FBAccountAlreadyAssociateException() {
    }

    public FBAccountAlreadyAssociateException(String message) {
        super(message);
    }

    public FBAccountAlreadyAssociateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FBAccountAlreadyAssociateException(Throwable cause) {
        super(cause);
    }

    public FBAccountAlreadyAssociateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
