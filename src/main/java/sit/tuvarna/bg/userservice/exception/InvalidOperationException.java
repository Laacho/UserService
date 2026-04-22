package sit.tuvarna.bg.userservice.exception;

/**
 * Exception thrown when an invalid operation is performed.
 */
public class InvalidOperationException extends UserServiceException {
    public InvalidOperationException(String message) {
        super(message, "INVALID_OPERATION");
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, "INVALID_OPERATION", cause);
    }
}

