package exceptions;

public class SQLCustomException extends RuntimeException {
    public SQLCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}