package sit.tuvarna.bg.userservice.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.userservice.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-accounts")
public class AccountController {
    
    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/{userId}/link")
    public ResponseEntity<Void> linkAccount(@PathVariable UUID userId,@RequestBody UUID accountId) {
        userService.linkAccount(userId, accountId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<UUID>> getAllAccountsForUser(HttpServletRequest request) {
        List<UUID> allAccounts = userService.getAllAccounts(request);
        return ResponseEntity.ok(allAccounts);
    }

    @GetMapping("/{userId}/account/{accountId}/validate")
    public ResponseEntity<Boolean> get(@PathVariable UUID userId, @PathVariable UUID accountId) {
        Boolean result = userService.validateAccount(userId, accountId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}/account/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID userId, @PathVariable UUID accountId) {
        userService.deleteAccount(userId,accountId);
        return ResponseEntity.ok().build();
    }
}
