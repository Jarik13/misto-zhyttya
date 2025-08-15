package org.example.mediaservice.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO відповіді, що містить ключ медіа та presigned URL")
public record MediaResponse(
        @Schema(description = "Унікальний ключ об’єкта медіа в S3", example = "123e4567-e89b-12d3-a456-426614174000-avatar.png")
        String key,

        @Schema(description = "Presigned URL для доступу до медіа", example = "https://bucket.s3.amazonaws.com/123e4567-e89b-12d3-a456-426614174000-avatar.png?X-Amz-Algorithm=AWS4-HMAC-SHA256...")
        String presignedUrl
) {}
