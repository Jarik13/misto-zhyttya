package org.example.mediaservice.dto;

public record MediaResponse(
        String key,
        String presignedUrl
) {
}
