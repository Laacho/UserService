package sit.tuvarna.bg.userservice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.tuvarna.bg.userservice.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalUserController {


    private final UserService userService;

    @Autowired
    public InternalUserController(UserService userService) {
        this.userService = userService;

    }

    @GetMapping("/users/{userId}/exists")
    public ResponseEntity<Void> userExists(@PathVariable UUID userId) {
        boolean isPresent = userService.checkIfUserExists(userId);
        if (isPresent) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Epoch millis before which refresh tokens are invalid. 0 = no gate.
    // Consumed by auth-service on the refresh path to enforce session invalidation.
    @GetMapping("/users/{userId}/tokens-valid-from")
    public ResponseEntity<Long> tokensValidFrom(@PathVariable UUID userId) {
        java.time.Instant validFrom = userService.getTokensValidFrom(userId);
        return ResponseEntity.ok(validFrom == null ? 0L : validFrom.toEpochMilli());
    }
}
