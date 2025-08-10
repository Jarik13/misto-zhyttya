package org.example.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.mediaservice.dto.MediaResponse;
import org.example.mediaservice.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(MultipartFile multipartFile) throws IOException {
        return ResponseEntity.ok(mediaService.uploadMedia(multipartFile));
    }
}
