package exceptions;

public class ChefServiceException extends Exception {
    public ChefServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
