package sit.tuvarna.bg.userservice.web.dto.request;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssueRequest {
    private UUID userId;
    private String username;
    private Set<String> roles;
}
