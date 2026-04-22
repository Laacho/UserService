package sit.tuvarna.bg.userservice.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssueRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;
    @NotBlank(message = "Username is required")
    private String username;
    @NotEmpty(message = "Roles cannot be empty")
    private Set<String> roles;
}
