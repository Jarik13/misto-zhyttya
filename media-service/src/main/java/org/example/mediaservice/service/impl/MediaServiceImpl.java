package org.example.mediaservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.mediaservice.dto.MediaResponse;
import org.example.mediaservice.model.Media;
import org.example.mediaservice.repository.MediaRepository;
import org.example.mediaservice.service.MediaService;
import org.example.mediaservice.service.S3PresignedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private final S3Client s3Client;
    private final MediaRepository mediaRepository;
    private final S3PresignedService s3PresignedService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    @Transactional
    public MediaResponse uploadMedia(MultipartFile multipartFile) throws IOException {
        String key = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(multipartFile.getContentType())
                        .build(),
                RequestBody.fromBytes(multipartFile.getBytes())
        );

        Media media = Media.builder()
                .filename(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .key(key)
                .size(multipartFile.getSize())
                .build();

        mediaRepository.save(media);

        return getMediaWithPresignedUrl(media.getKey());
    }

    @Override
    public MediaResponse getMediaWithPresignedUrl(String key) {
        Media media = mediaRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Media not found with key: " + key));

        return new MediaResponse(key, s3PresignedService.generatePresignedUrl(media.getKey()));
    }
}
