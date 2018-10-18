package ua.kiev.prog.photopond.twitter.Exception;

public class TweetPublishingException extends CustomTwitterException {

    private static final long serialVersionUID = 225713309134367079L;

    public TweetPublishingException() {
    }

    public TweetPublishingException(String message) {
        super(message);
    }

    public TweetPublishingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TweetPublishingException(Throwable cause) {
        super(cause);
    }

    public TweetPublishingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
