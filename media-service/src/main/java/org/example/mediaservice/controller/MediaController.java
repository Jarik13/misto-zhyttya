package org.example.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.mediaservice.dto.MediaResponse;
import org.example.mediaservice.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<MediaResponse> getMedia(@RequestParam String key) throws IOException {
        return ResponseEntity.ok(mediaService.getMediaWithPresignedUrl(key));
    }

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(@RequestParam(value = "mediaFile") MultipartFile mediaFile) throws IOException {
        return ResponseEntity.ok(mediaService.uploadMedia(mediaFile));
    }
}
