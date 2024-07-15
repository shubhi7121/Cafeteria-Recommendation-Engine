package exceptions;

public class ProfileUpdateException extends Exception {
    public ProfileUpdateException(String message) {
        super(message);
    }

    public ProfileUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
