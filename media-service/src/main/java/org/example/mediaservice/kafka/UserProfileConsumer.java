package org.example.mediaservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.event.AvatarEvent;
import org.example.mediaservice.kafka.strategy.AvatarEventHandlerFactory;
import org.example.mediaservice.model.Status;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileConsumer {
    private final AvatarEventHandlerFactory avatarEventHandlerFactory;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-profile-avatar-events", groupId = "media-service")
    public void consumeAvatarEvent(byte[] payload) {
        try {
            AvatarEvent event = objectMapper.readValue(payload, AvatarEvent.class);
            log.info("Received avatar event: action={}, key={}", event.action(), event.key());

            avatarEventHandlerFactory.getHandler(Status.valueOf(event.action()))
                    .handle(new AvatarEvent(event.action(), event.key()));
            log.info("Successfully consumed avatar event");
        } catch (Exception ex) {
            log.error("Failed to process avatar event: {}", ex.getMessage(), ex);
        }
    }
}
