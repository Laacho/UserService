package sit.tuvarna.bg.userservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import sit.tuvarna.bg.userservice.config.KafkaTopicsProperties;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    /**
     * @param eventType dot-notation type, e.g. "user.registered"
     * @param key       partition key — userId (or username when userId is unknown)
     * @param payload   the event-specific record
     */
    public void publish(String eventType, String key, Object payload) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", UUID.randomUUID().toString());
        envelope.put("eventType", eventType);
        envelope.put("sourceService", "user-service");
        envelope.put("timestamp", Instant.now().toString());
        envelope.put("correlationId", UUID.randomUUID().toString());
        envelope.put("version", 1);
        envelope.put("payload", payload);

        kafkaTemplate.send(topics.getUserEvents(), key, envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {}: {}", eventType, ex.getMessage(), ex);
                    } else {
                        log.info("Published {} key={} offset={}",
                                eventType, key, result.getRecordMetadata().offset());
                    }
                });
    }
}