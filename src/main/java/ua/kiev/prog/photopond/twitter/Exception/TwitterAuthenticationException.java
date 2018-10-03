package ua.kiev.prog.photopond.twitter.Exception;

public class TwitterAuthenticationException extends CustomTwitterException {

    private static final long serialVersionUID = 8947378984552979338L;

    public TwitterAuthenticationException() {
    }

    public TwitterAuthenticationException(String message) {
        super(message);
    }

    public TwitterAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TwitterAuthenticationException(Throwable cause) {
        super(cause);
    }

    public TwitterAuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
