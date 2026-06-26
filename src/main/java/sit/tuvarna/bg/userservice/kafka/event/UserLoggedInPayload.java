package sit.tuvarna.bg.userservice.kafka.event;

import java.util.UUID;

public record UserLoggedInPayload(
        UUID userId,
        String loginTimestamp   // ISO-8601
) {}