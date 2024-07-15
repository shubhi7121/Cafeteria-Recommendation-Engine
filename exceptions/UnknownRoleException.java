package exceptions;

public class UnknownRoleException extends RuntimeException {
    public UnknownRoleException(String role) {
        super("Unknown role: " + role + ". Access denied.");
    }
}
