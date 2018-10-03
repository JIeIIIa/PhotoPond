package ua.kiev.prog.photopond.twitter.Exception;

public class TwitterAccountAlreadyAssociateException extends AssociateTwitterAccountException {

    private static final long serialVersionUID = 7970091141993184841L;

    public TwitterAccountAlreadyAssociateException() {
    }

    public TwitterAccountAlreadyAssociateException(String message) {
        super(message);
    }

    public TwitterAccountAlreadyAssociateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TwitterAccountAlreadyAssociateException(Throwable cause) {
        super(cause);
    }

    public TwitterAccountAlreadyAssociateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
