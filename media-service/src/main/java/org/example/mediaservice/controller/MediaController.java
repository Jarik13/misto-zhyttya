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
@Tag(name = "Media", description = "Operations related to media upload and retrieval")
public class MediaController {
    private final MediaService mediaService;

    @Operation(summary = "Get presigned URL for media by key",
            description = "Returns the media's presigned URL using the media key",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Presigned URL successfully retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MediaResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Media not found", content = @Content)
            })
    @GetMapping
    public ResponseEntity<MediaResponse> getMedia(
            @Parameter(description = "Key of the media file", required = true, example = "123e4567-e89b-12d3-a456-426614174000-avatar.png")
            @RequestParam String key
    ) throws IOException {
        return ResponseEntity.ok(mediaService.getMediaWithPresignedUrl(key));
    }

    @Operation(summary = "Upload a media file",
            description = "Uploads a media file and returns its key and presigned URL",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Media successfully uploaded",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MediaResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid file input", content = @Content)
            })
    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(
            @Parameter(description = "Media file to upload", required = true)
            @RequestParam(value = "mediaFile") MultipartFile mediaFile
    ) throws IOException {
        return ResponseEntity.ok(mediaService.uploadMedia(mediaFile));
    }
}
