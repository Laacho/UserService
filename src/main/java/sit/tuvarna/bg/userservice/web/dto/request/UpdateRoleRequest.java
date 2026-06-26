package sit.tuvarna.bg.userservice.web.dto.request;

import jakarta.validation.constraints.NotNull;
import sit.tuvarna.bg.userservice.user.model.Roles;

/**
 * Admin request to set a user's role explicitly. Covers both promotion and demotion —
 * the supplied {@code role} fully replaces the target user's current role.
 */
public record UpdateRoleRequest(
        @NotNull Roles role
) {
}