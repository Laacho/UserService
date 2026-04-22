package sit.tuvarna.bg.userservice.exception;

/**
 * Exception thrown when user is not found.
 */
public class UserNotFoundException extends UserServiceException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}

