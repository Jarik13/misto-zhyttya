package org.example.mediaservice.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO containing media key and presigned URL")
public record MediaResponse(
        @Schema(description = "Unique key of the media object in S3", example = "123e4567-e89b-12d3-a456-426614174000-avatar.png")
        String key,

        @Schema(description = "Presigned URL to access the media", example = "https://bucket.s3.amazonaws.com/123e4567-e89b-12d3-a456-426614174000-avatar.png?X-Amz-Algorithm=AWS4-HMAC-SHA256...")
        String presignedUrl
) {}
