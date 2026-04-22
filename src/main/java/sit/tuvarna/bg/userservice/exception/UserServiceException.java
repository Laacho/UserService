package sit.tuvarna.bg.userservice.exception;

import lombok.Getter;

/**
 * Base exception class for UserService.
 * All business logic exceptions should extend from this class.
 */
@Getter
public class UserServiceException extends RuntimeException {
    private final String errorCode;

    public UserServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}

