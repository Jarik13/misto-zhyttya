package org.example.mediaservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.mediaservice.service.MediaService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileConsumer {
    private final MediaService mediaService;

    @KafkaListener(topics = "user-profile-delete-avatar", groupId = "media-service")
    public void consumeDeletionUserAvatar(String key) {
        log.info("Received request to delete avatar with key: {}", key);

        try {
            mediaService.deleteMediaByKey(key);
            log.info("Successfully deleted media with key: {}", key);
        } catch (Exception ex) {
            log.error("Failed to delete media with key {}: {}", key, ex.getMessage());
        }
    }
}
