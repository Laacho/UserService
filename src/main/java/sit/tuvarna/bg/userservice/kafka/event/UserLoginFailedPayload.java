package sit.tuvarna.bg.userservice.kafka.event;

import java.util.UUID;

public record UserLoginFailedPayload(
        UUID userId,     // nullable when reason == USER_NOT_FOUND
        String username,
        String reason,   // INVALID_PASSWORD | USER_NOT_FOUND
        String ipAddress // nullable — see note in Section 7.3
) {}