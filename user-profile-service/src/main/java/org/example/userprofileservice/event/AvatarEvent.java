package org.example.userprofileservice.event;

public record AvatarEvent(
        String action,
        String key
) {
}
