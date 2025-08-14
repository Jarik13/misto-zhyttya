package org.example.mediaservice.kafka.strategy.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.dto.media.AvatarEvent;
import org.example.mediaservice.kafka.strategy.AvatarEventHandler;
import org.example.mediaservice.model.Status;
import org.example.mediaservice.service.MediaService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateAvatarStatusHandler implements AvatarEventHandler {
    private final MediaService mediaService;

    @Override
    public void handle(AvatarEvent event) {
        mediaService.updateMediaStatus(event.action(), event.key());
        log.info("Updated media status for key {} to {}", event.key(), event.action());
    }

    @Override
    public Status getStatus() {
        return Status.APPROVED;
    }
}
