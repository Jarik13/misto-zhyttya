package org.example.mediaservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.dto.media.AvatarEvent;
import org.example.mediaservice.kafka.strategy.AvatarEventHandlerFactory;
import org.example.mediaservice.model.Status;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileConsumer {
    private final AvatarEventHandlerFactory avatarEventHandlerFactory;

    @KafkaListener(topics = "user-profile-avatar-events", groupId = "media-service")
    public void consumeAvatarEvent(String action, String key) {
        log.info("Received avatar event: action={}, key={}", action, key);

        try {
            avatarEventHandlerFactory.getHandler(Status.valueOf(action)).handle(new AvatarEvent(action, key));
            log.info("Successfully consumed avatar event");
        } catch (Exception ex) {
            log.error("Failed to do avatar event with key {}: {}", key, ex.getMessage());
        }
    }
}
