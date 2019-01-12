package ua.kiev.prog.photopond.twitter.exception;

public class DisassociateTwitterAccountException extends CustomTwitterException {
    
    private static final long serialVersionUID = -8237059736599782707L;
    
    public DisassociateTwitterAccountException() {
    }

    public DisassociateTwitterAccountException(String message) {
        super(message);
    }

    public DisassociateTwitterAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisassociateTwitterAccountException(Throwable cause) {
        super(cause);
    }

    public DisassociateTwitterAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
