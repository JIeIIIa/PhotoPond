package ua.kiev.prog.photopond.twitter.Exception;

public class AssociateTwitterAccountException extends CustomTwitterException {

    private static final long serialVersionUID = 5945846699421649436L;

    public AssociateTwitterAccountException() {
    }

    public AssociateTwitterAccountException(String message) {
        super(message);
    }

    public AssociateTwitterAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssociateTwitterAccountException(Throwable cause) {
        super(cause);
    }

    public AssociateTwitterAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }    
}
