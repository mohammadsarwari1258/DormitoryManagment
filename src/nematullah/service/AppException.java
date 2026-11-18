package nematullah.service;

/** An error whose message is safe and clear enough to show directly to the user. */
public class AppException extends Exception {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
