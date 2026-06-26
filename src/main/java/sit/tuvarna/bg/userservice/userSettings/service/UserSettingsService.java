package sit.tuvarna.bg.userservice.userSettings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.tuvarna.bg.userservice.aop.Loggable;
import sit.tuvarna.bg.userservice.config.EncryptionService;
import sit.tuvarna.bg.userservice.exception.*;
import sit.tuvarna.bg.userservice.kafka.UserEventPublisher;
import sit.tuvarna.bg.userservice.kafka.event.UserSettingsUpdatedPayload;
import sit.tuvarna.bg.userservice.user.model.User;
import sit.tuvarna.bg.userservice.user.repository.UserRepository;
import sit.tuvarna.bg.userservice.userSettings.model.TwoFactorMethod;
import sit.tuvarna.bg.userservice.userSettings.model.UserSettings;
import sit.tuvarna.bg.userservice.userSettings.repository.UserSettingsRepository;
import sit.tuvarna.bg.userservice.web.dto.request.UserSettingsUpdateRequest;
import sit.tuvarna.bg.userservice.web.dto.response.UserSettingsResponse;

import java.util.UUID;

@Service
public class UserSettingsService {
    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final UserEventPublisher eventPublisher;

    @Autowired
    public UserSettingsService(UserSettingsRepository userSettingsRepository, UserRepository userRepository, EncryptionService encryptionService, UserEventPublisher eventPublisher) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.eventPublisher = eventPublisher;
    }

    @Loggable
    public UserSettingsResponse updateSettings(UUID userId, UserSettingsUpdateRequest request) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (request.getEmailNotificationsEnabled() != null)
            settings.setEmailNotificationEnabled(request.getEmailNotificationsEnabled());

        if (request.getInternalNotificationsEnabled() != null)
            settings.setInternalNotificationEnabled(request.getInternalNotificationsEnabled());

        // 2FA state is NOT mutable here. Enabling must go through /2fa/setup → /2fa/verify
        // (enableTwoFactor, which requires a valid TOTP code). Allowing it via this
        // unauthenticated-by-TOTP settings PUT would let a user enable 2FA with no secret
        // stored (locking themselves out) or silently disable it. See storeTwoFactorSecret.

        userSettingsRepository.save(settings);

        eventPublisher.publish(
                "user.settings-updated",
                userId.toString(),
                new UserSettingsUpdatedPayload(
                        userId,
                        settings.isEmailNotificationEnabled(),
                        settings.isInternalNotificationEnabled(),
                        settings.isTwoFactorEnabled()
                ));

        return toResponse(settings);
    }
    @Loggable
    @Transactional
    public void storeTwoFactorSecret(UUID userId, String rawSecret) {
        String encrypted = encryptionService.encrypt(rawSecret);

        // @Transactional makes this read-modify-write atomic (was 3 separate tx).
        // True protection against concurrent same-user setup is the frontend guard
        // in TwoFASetupPage; here a single call is now clean.
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setTwoFactorSecret(encrypted);
        settings.setTwoFactorMethod(TwoFactorMethod.TOTP);
        // NOT enabled here — only after the user proves a valid TOTP code via /2fa/verify
        // (enableTwoFactor). Enabling at setup time would lock out users who abandon setup.

        userSettingsRepository.save(settings);
    }
    @Loggable
    public UserSettingsResponse getSettings(UUID userId){
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return toResponse(settings);
    }
    @Loggable
    public String getTwoFactorSecret(UUID userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User settings not found for user '" + userId + "'"));

        if (settings.getTwoFactorSecret() == null)
            throw new InvalidOperationException("Two-factor authentication secret not configured for this user");

        return encryptionService.decrypt(settings.getTwoFactorSecret());
    }
    @Loggable
    public void enableTwoFactor(UUID userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User settings not found for user '" + userId + "'"));

        settings.setTwoFactorEnabled(true);
        userSettingsRepository.save(settings);
    }

    private UserSettingsResponse toResponse(UserSettings settings) {
        return UserSettingsResponse.builder()
                .emailNotificationsEnabled(settings.isEmailNotificationEnabled())
                .internalNotificationsEnabled(settings.isInternalNotificationEnabled())
                .twoFactorEnabled(settings.isTwoFactorEnabled())
                .twoFactorMethod(settings.getTwoFactorMethod())
                .build();
    }

    @Loggable
    private UserSettings createDefaultSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + userId + "' not found"));
        UserSettings settings = UserSettings.builder()
                .user(user)
                .emailNotificationEnabled(false)
                .internalNotificationEnabled(true)
                .twoFactorEnabled(false)
                .twoFactorMethod(TwoFactorMethod.UNDEFINED)
                .build();
        return userSettingsRepository.save(settings);
    }
}
