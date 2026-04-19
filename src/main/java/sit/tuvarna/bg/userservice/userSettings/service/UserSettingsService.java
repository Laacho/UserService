package sit.tuvarna.bg.userservice.userSettings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.userservice.config.EncryptionService;
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
    public void storeTwoFactorSecret(UUID userId, String rawSecret) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        String encrypted = encryptionService.encrypt(rawSecret);

        settings.setTwoFactorSecret(encrypted);
        settings.setTwoFactorMethod(TwoFactorMethod.TOTP);
        settings.setTwoFactorEnabled(true);

        userSettingsRepository.save(settings);
    }
    public UserSettingsResponse getSettings(UUID userId){
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return toResponse(settings);
    }
    public String getTwoFactorSecret(UUID userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User settings not found"));

        if (settings.getTwoFactorSecret() == null)
            throw new IllegalStateException("2FA secret not set");

        return encryptionService.decrypt(settings.getTwoFactorSecret());
    }

    public void enableTwoFactor(UUID userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User settings not found"));

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

    private UserSettings createDefaultSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));
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
