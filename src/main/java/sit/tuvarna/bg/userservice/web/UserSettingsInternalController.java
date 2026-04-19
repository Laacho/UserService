package sit.tuvarna.bg.userservice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.tuvarna.bg.userservice.userSettings.service.UserSettingsService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal")
public class UserSettingsInternalController {

    private final UserSettingsService userSettingsService;

    @Autowired
    public UserSettingsInternalController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }


    @PostMapping("/{userId}/2fa-secret")
    public void storeSecret(@PathVariable UUID userId,@RequestBody String secret){
        userSettingsService.storeTwoFactorSecret(userId,secret);
    }

    @GetMapping("/{userId}/2fa-secret")
    public ResponseEntity<String> getSecret(@PathVariable UUID userId) {
        String twoFactorSecret = userSettingsService.getTwoFactorSecret(userId);
        return  ResponseEntity.ok(twoFactorSecret);
    }

    @PostMapping("/{userId}/2fa/enable")
    public void enable2fa(@PathVariable UUID userId) {
        userSettingsService.enableTwoFactor(userId);
        
    }
}
