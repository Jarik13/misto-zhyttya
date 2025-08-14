package org.example.userprofileservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void approveUserAvatar(String key) {
        log.info("Approving user avatar for key {}", key);

        try {
            kafkaTemplate.send("user-profile-approve-avatar", key);
        } catch (Exception ex) {
            log.error("Error sending user-profile-approve-avatar: {}", ex.getMessage());
        }
    }

    public void deleteUserAvatar(String key) {
        log.info("Delete user avatar with key = {}", key);

        try {
            kafkaTemplate.send("user-profile-delete-avatar", key);
        } catch (Exception ex) {
            log.error("Error sending user-profile-delete-avatar: {}", ex.getMessage());
        }
    }
}
