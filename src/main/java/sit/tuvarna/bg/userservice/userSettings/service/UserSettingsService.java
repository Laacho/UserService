package sit.tuvarna.bg.userservice.userSettings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.userservice.aop.Loggable;
import sit.tuvarna.bg.userservice.config.EncryptionService;
import sit.tuvarna.bg.userservice.exception.*;
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

    @Autowired
    public UserSettingsService(UserSettingsRepository userSettingsRepository, UserRepository userRepository, EncryptionService encryptionService) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Loggable
    public UserSettingsResponse updateSettings(UUID userId, UserSettingsUpdateRequest request) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (request.getEmailNotificationsEnabled() != null)
            settings.setEmailNotificationEnabled(request.getEmailNotificationsEnabled());

        if (request.getInternalNotificationsEnabled() != null)
            settings.setInternalNotificationEnabled(request.getInternalNotificationsEnabled());

        if (request.getTwoFactorEnabled() != null)
            settings.setTwoFactorEnabled(request.getTwoFactorEnabled());

        if (request.getTwoFactorMethod() != null)
            settings.setTwoFactorMethod(request.getTwoFactorMethod());

        userSettingsRepository.save(settings);
        return toResponse(settings);
    }
    @Loggable
    public void storeTwoFactorSecret(UUID userId, String rawSecret) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        String encrypted = encryptionService.encrypt(rawSecret);

        settings.setTwoFactorSecret(encrypted);
        settings.setTwoFactorMethod(TwoFactorMethod.TOTP);
        settings.setTwoFactorEnabled(true);

        userSettingsRepository.save(settings);
    }
    @Loggable
    public UserSettingsResponse getSettings(UUID userId){
        UserSettings settings = userSettingsRepository.findById(userId)
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
