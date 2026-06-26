package sit.tuvarna.bg.userservice.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.userservice.exception.InvalidOperationException;
import sit.tuvarna.bg.userservice.user.service.UserService;
import sit.tuvarna.bg.userservice.utils.JwtValidator;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-accounts")
public class AccountController {

    private final UserService userService;
    private final JwtValidator jwtValidator;

    @Autowired
    public AccountController(UserService userService, JwtValidator jwtValidator) {
        this.userService = userService;
        this.jwtValidator = jwtValidator;
    }


    @PostMapping("/{userId}/link")
    public ResponseEntity<Void> linkAccount(@PathVariable UUID userId, @RequestBody UUID accountId,
                                            HttpServletRequest request) {
        assertOwner(request, userId);
        userService.linkAccount(userId, accountId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<UUID>> getAllAccountsForUser(HttpServletRequest request) {
        List<UUID> allAccounts = userService.getAllAccounts(request);
        return ResponseEntity.ok(allAccounts);
    }

    @GetMapping("/{userId}/account/{accountId}/validate")
    public ResponseEntity<Boolean> get(@PathVariable UUID userId, @PathVariable UUID accountId,
                                       HttpServletRequest request) {
        assertOwner(request, userId);
        Boolean result = userService.validateAccount(userId, accountId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}/account/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID userId, @PathVariable UUID accountId,
                                              HttpServletRequest request) {
        assertOwner(request, userId);
        userService.deleteAccount(userId, accountId);
        return ResponseEntity.ok().build();
    }

    /** Ensures the authenticated token's userId matches the path userId (prevents IDOR). */
    private void assertOwner(HttpServletRequest request, UUID pathUserId) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidOperationException("Invalid or missing Authorization header");
        }
        UUID tokenUserId = jwtValidator.extractUserId(header.substring(7));
        if (!tokenUserId.equals(pathUserId)) {
            throw new InvalidOperationException("Cannot access another user's accounts");
        }
    }
}
