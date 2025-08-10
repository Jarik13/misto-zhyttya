package org.example.mediaservice.service;

import org.example.mediaservice.dto.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MediaService {
    MediaResponse uploadMedia(MultipartFile multipartFile) throws IOException;
    MediaResponse getPresignedUrl(String mediaId);
}
