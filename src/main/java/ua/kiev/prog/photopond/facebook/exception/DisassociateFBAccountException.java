package ua.kiev.prog.photopond.facebook.exception;

public class DisassociateFBAccountException extends FBException {

    private static final long serialVersionUID = 7776151911546422599L;

    public DisassociateFBAccountException() {
    }

    public DisassociateFBAccountException(String message) {
        super(message);
    }

    public DisassociateFBAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisassociateFBAccountException(Throwable cause) {
        super(cause);
    }

    public DisassociateFBAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
