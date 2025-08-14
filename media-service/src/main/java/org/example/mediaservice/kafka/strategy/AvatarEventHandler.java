package org.example.mediaservice.kafka.strategy;

import org.example.mediaservice.dto.media.AvatarEvent;
import org.example.mediaservice.model.Status;

public interface AvatarEventHandler {
    void handle(AvatarEvent event);
    Status getStatus();
}
