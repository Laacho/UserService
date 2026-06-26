package sit.tuvarna.bg.userservice.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.userservice.user.model.User;
import sit.tuvarna.bg.userservice.user.service.UserService;
import sit.tuvarna.bg.userservice.utils.JwtValidator;
import sit.tuvarna.bg.userservice.web.dto.request.UpdateRoleRequest;
import sit.tuvarna.bg.userservice.web.dto.response.UserRoleResponse;

import java.util.UUID;

/**
 * Admin-only user management. Access is restricted to the ADMIN authority in SecurityConfig
 * (path {@code /api/v1/admin/**}). Covers promotion and demotion via an explicit target role.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final JwtValidator jwtValidator;

    @Autowired
    public AdminUserController(UserService userService, JwtValidator jwtValidator) {
        this.userService = userService;
        this.jwtValidator = jwtValidator;
    }

    /**
     * Promote or demote a user by setting their role explicitly.
     * The acting admin cannot change their own role.
     */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserRoleResponse> updateRole(@PathVariable UUID userId,
                                                       @RequestBody @Valid UpdateRoleRequest request,
                                                       HttpServletRequest httpRequest) {
        // Token already validated by JwtAuthenticationFilter; extractUserId parses the claim.
        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID actingUserId = jwtValidator.extractUserId(token);
        User updated = userService.updateRole(actingUserId, userId, request.role());
        return ResponseEntity.ok(new UserRoleResponse(updated.getId(), updated.getUsername(), updated.getRole()));
    }
}