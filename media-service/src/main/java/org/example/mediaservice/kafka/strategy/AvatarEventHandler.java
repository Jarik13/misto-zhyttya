package org.example.mediaservice.kafka.strategy;

import org.example.mediaservice.dto.media.AvatarEvent;

public interface AvatarEventHandler {
    void handle(AvatarEvent event);
}
