package sit.tuvarna.bg.userservice.web.dto.response;

import sit.tuvarna.bg.userservice.user.model.Roles;

import java.util.UUID;

/**
 * Returned after an admin changes a user's role.
 */
public record UserRoleResponse(
        UUID id,
        String username,
        Roles role
) {
}