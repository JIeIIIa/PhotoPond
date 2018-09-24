package ua.kiev.prog.photopond.facebook.Exception;

public class FBAuthenticationException extends FBException {

    private static final long serialVersionUID = 6983176614514538867L;

    public FBAuthenticationException() {
    }

    public FBAuthenticationException(String message) {
        super(message);
    }

    public FBAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FBAuthenticationException(Throwable cause) {
        super(cause);
    }

    public FBAuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
