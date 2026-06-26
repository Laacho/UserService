package sit.tuvarna.bg.userservice.kafka.event;

import java.util.UUID;

public record UserSettingsUpdatedPayload(
        UUID userId,
        boolean emailNotifications,
        boolean internalNotifications,
        boolean twoFactorEnabled
) {}