package ua.kiev.prog.photopond.twitter.Exception;

public class NotFoundTwitterAssociatedAccountException extends TweetPublishingException {

    private static final long serialVersionUID = 8152904259207798491L;

    public NotFoundTwitterAssociatedAccountException() {
    }

    public NotFoundTwitterAssociatedAccountException(String message) {
        super(message);
    }

    public NotFoundTwitterAssociatedAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundTwitterAssociatedAccountException(Throwable cause) {
        super(cause);
    }

    public NotFoundTwitterAssociatedAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
