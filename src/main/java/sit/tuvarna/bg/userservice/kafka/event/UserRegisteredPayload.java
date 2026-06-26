package sit.tuvarna.bg.userservice.kafka.event;

import java.util.UUID;

public record UserRegisteredPayload(
        UUID userId,
        String username,
        String email,
        String role
) {}