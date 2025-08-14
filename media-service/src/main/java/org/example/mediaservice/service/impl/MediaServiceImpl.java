package org.example.mediaservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.dto.error.ErrorCode;
import org.example.mediaservice.dto.media.MediaResponse;
import org.example.mediaservice.exception.BusinessException;
import org.example.mediaservice.model.Media;
import org.example.mediaservice.model.Status;
import org.example.mediaservice.repository.MediaRepository;
import org.example.mediaservice.service.MediaService;
import org.example.mediaservice.service.S3PresignedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
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
                .status(Status.PENDING)
                .build();

        mediaRepository.save(media);

        return getMediaWithPresignedUrl(media.getKey());
    }

    @Override
    @Transactional
    public void deleteMediaByKey(String key) {
        Media media = mediaRepository.findByKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Media not found with key: " + key));

        deleteObjectFromS3(key);

        mediaRepository.delete(media);

        log.info("Deleted media with key: {}", key);
    }

    @Override
    public MediaResponse getMediaWithPresignedUrl(String key) {
        Media media = mediaRepository.findByKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Media not found with key: " + key));

        return new MediaResponse(key, s3PresignedService.generatePresignedUrl(media.getKey()));
    }

    @Override
    public void updateMediaStatus(String action, String key) {
        mediaRepository.findByKey(key).ifPresent(media -> {
            media.setStatus(Status.valueOf(action));
            mediaRepository.save(media);
        });
    }

    @Override
    public void deleteMedia(Status status) {
        List<Media> mediaList = mediaRepository.findAll()
                .stream()
                .filter(media -> media.getStatus() == status)
                .toList();

        for (Media media : mediaList) {
            try {
                deleteObjectFromS3(media.getKey());
            } catch (Exception e) {
                log.error("Failed to delete S3 object with key {}: {}", media.getKey(), e.getMessage());
            }
        }

        mediaRepository.deleteAll(mediaList);
        log.info("Deleted {} media records with status {}", mediaList.size(), status);
    }

    private void deleteObjectFromS3(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        log.info("Deleted S3 object with key: {}", key);
    }
}
