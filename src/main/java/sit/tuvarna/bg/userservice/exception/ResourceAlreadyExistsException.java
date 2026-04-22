package sit.tuvarna.bg.userservice.exception;

/**
 * Exception thrown when a resource already exists (e.g., user with given email).
 */
public class ResourceAlreadyExistsException extends UserServiceException {
    public ResourceAlreadyExistsException(String message) {
        super(message, "RESOURCE_ALREADY_EXISTS");
    }
}

