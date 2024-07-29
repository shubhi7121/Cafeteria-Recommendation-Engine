package exceptions;

public class AdminServiceException extends Exception {
    public AdminServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
