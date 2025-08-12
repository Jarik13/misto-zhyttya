package org.example.mediaservice.service;

import org.example.mediaservice.dto.media.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MediaService {
    MediaResponse uploadMedia(MultipartFile multipartFile) throws IOException;
    void deleteMediaByKey(String key);
    MediaResponse getMediaWithPresignedUrl(String mediaId);
}
