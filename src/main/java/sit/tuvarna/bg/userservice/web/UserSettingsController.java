package sit.tuvarna.bg.userservice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.userservice.user.service.UserService;
import sit.tuvarna.bg.userservice.userSettings.service.UserSettingsService;
import sit.tuvarna.bg.userservice.web.dto.request.UserSettingsUpdateRequest;
import sit.tuvarna.bg.userservice.web.dto.response.UserSettingsResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;
    private final UserService userService;

    @Autowired
    public UserSettingsController(UserSettingsService userSettingsService, UserService userService) {
        this.userSettingsService = userSettingsService;
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<UserSettingsResponse> getUserSpecificSettings(@AuthenticationPrincipal String username) {
        UUID userId = userService.resolveUserId(username);
        UserSettingsResponse settings = userSettingsService.getSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public UserSettingsResponse updateMySettings(
            @AuthenticationPrincipal String username,
            @RequestBody UserSettingsUpdateRequest request
    ) {
        UUID userId = userService.resolveUserId(username);
        return userSettingsService.updateSettings(userId, request);
    }
}
