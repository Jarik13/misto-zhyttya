package org.example.userprofileservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userprofileservice.event.AvatarEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileProducer {
    private final KafkaTemplate<String, AvatarEvent> kafkaTemplate;
    private static final String TOPIC = "user-profile-avatar-events";

    public void sendAvatarEvent(String action, String key) {
        log.info("Sending avatar event: action={}, key={}", action, key);
        try {
            kafkaTemplate.send(TOPIC, new AvatarEvent(action, key));
        } catch (Exception ex) {
            log.error("Error sending avatar event: {}", ex.getMessage());
        }
    }
}

