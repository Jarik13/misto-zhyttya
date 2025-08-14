package org.example.mediaservice.kafka.strategy;

import org.example.mediaservice.event.AvatarEvent;
import org.example.mediaservice.model.Status;

public interface AvatarEventHandler {
    void handle(AvatarEvent event);
    Status getStatus();
}
