package sit.tuvarna.bg.userservice.exception;

/**
 * Exception thrown when authentication fails (invalid credentials, etc).
 */
public class AuthenticationException extends UserServiceException {
    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}

