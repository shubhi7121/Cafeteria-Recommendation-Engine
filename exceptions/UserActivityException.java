package exceptions;

public class UserActivityException extends Exception {
    public UserActivityException(String message, Throwable cause) {
        super(message, cause);
    }
}
