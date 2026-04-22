package sit.tuvarna.bg.userservice.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Validation error response DTO for field validation errors.
 * Contains a list of field-level errors.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {
    private String errorCode;
    private String message;
    private Long timestamp;
    private Integer status;
    private String path;
    private List<FieldError> fieldErrors;

    /**
     * Represents a single field validation error.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}

