package org.example.mediaservice.kafka.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.mediaservice.model.Status;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AvatarEventHandlerFactory {
    private final List<AvatarEventHandler> handlers;
    private final Map<Status, AvatarEventHandler> handlerMap = new EnumMap<>(Status.class);

    @PostConstruct
    public void init() {
        for (AvatarEventHandler handler : handlers) {
            handlerMap.put(handler.getStatus(), handler);
        }
    }

    public AvatarEventHandler getHandler(Status status) {
        return handlerMap.get(status);
    }
}
