package org.example.mediaservice.kafka.strategy.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.dto.media.AvatarEvent;
import org.example.mediaservice.kafka.strategy.AvatarEventHandler;
import org.example.mediaservice.model.Status;
import org.example.mediaservice.service.MediaService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAvatarHandler implements AvatarEventHandler {
    private final MediaService mediaService;

    @Override
    public void handle(AvatarEvent event) {
        mediaService.deleteMediaByKey(event.key());
        log.info("Successfully deleted media with key: {}", event.key());
    }

    @Override
    public Status getStatus() {
        return Status.DELETED;
    }
}
