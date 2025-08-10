package org.example.mediaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;
import org.example.mediaservice.dto.media.MediaResponse;
import org.example.mediaservice.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Медіа", description = "Операції, пов'язані з завантаженням і отриманням медіа файлів")
public class MediaController {
    private final MediaService mediaService;

    @Operation(summary = "Отримати presigned URL за ключем медіа",
            description = "Повертає presigned URL для доступу до медіа файлу за ключем",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Presigned URL успішно отримано",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MediaResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Медіа файл не знайдено", content = @Content)
            })
    @GetMapping
    public ResponseEntity<MediaResponse> getMedia(
            @Parameter(description = "Ключ медіа файлу", required = true, example = "123e4567-e89b-12d3-a456-426614174000-avatar.png")
            @RequestParam String key
    ) throws IOException {
        return ResponseEntity.ok(mediaService.getMediaWithPresignedUrl(key));
    }

    @Operation(summary = "Завантажити медіа файл",
            description = "Завантажує медіа файл та повертає його ключ і presigned URL",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Медіа файл успішно завантажено",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MediaResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Невірний вхідний файл", content = @Content)
            })
    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(
            @Parameter(description = "Файл медіа для завантаження", required = true)
            @RequestParam(value = "mediaFile") MultipartFile mediaFile
    ) throws IOException {
        return ResponseEntity.ok(mediaService.uploadMedia(mediaFile));
    }
}
