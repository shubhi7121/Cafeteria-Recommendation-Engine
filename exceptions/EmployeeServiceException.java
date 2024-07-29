package exceptions;

public class EmployeeServiceException extends Exception {
    public EmployeeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}