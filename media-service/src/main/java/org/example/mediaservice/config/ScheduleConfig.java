package org.example.mediaservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.model.Status;
import org.example.mediaservice.service.MediaService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {
    private final MediaService mediaService;

    @Scheduled(cron = "0 0 * * * *")
    public void deletePendingMedia() {
        log.info("Scheduled task started: deleting PENDING media");
        try {
            mediaService.deleteMedia(Status.PENDING);
        } catch (Exception e) {
            log.error("Error during scheduled deletion of PENDING media: {}", e.getMessage());
        }
    }
}
