package exceptions;

public class MaxAttemptsExceededException extends AuthenticationException {
    public MaxAttemptsExceededException() {
        super("Maximum authentication attempts reached. User blocked.");
    }
}
