package org.example.userprofileservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userprofileservice.event.AvatarEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileProducer {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "user-profile-avatar-events";

    public void sendAvatarEvent(String action, String key) {
        log.info("Sending avatar event: action={}, key={}", action, key);
        try {
            AvatarEvent event = new AvatarEvent(action, key);
            byte[] payload = objectMapper.writeValueAsBytes(event);
            kafkaTemplate.send(TOPIC, key, payload);
        } catch (Exception ex) {
            log.error("Error sending avatar event: {}", ex.getMessage(), ex);
        }
    }
}
