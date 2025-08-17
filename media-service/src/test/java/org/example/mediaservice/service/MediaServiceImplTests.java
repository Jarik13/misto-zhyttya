package org.example.mediaservice.service;

import org.example.mediaservice.dto.error.ErrorCode;
import org.example.mediaservice.dto.media.MediaResponse;
import org.example.mediaservice.exception.BusinessException;
import org.example.mediaservice.model.Media;
import org.example.mediaservice.model.Status;
import org.example.mediaservice.repository.MediaRepository;
import org.example.mediaservice.service.impl.MediaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaServiceImplTests {
    private S3Client s3Client;
    private MediaRepository mediaRepository;
    private S3PresignedService s3PresignedService;
    private MediaServiceImpl service;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        mediaRepository = mock(MediaRepository.class);
        s3PresignedService = mock(S3PresignedService.class);
        service = new MediaServiceImpl(s3Client, mediaRepository, s3PresignedService);

        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
    }

    @Test
    void givenMultipartFile_whenUploadMedia_thenSavesMediaAndReturnsResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello".getBytes()
        );

        String presignedUrl = "http://example.com/test.txt";
        when(s3PresignedService.generatePresignedUrl(anyString())).thenReturn(presignedUrl);

        doAnswer(invocation -> {
            Media savedMedia = invocation.getArgument(0, Media.class);
            when(mediaRepository.findByKey(savedMedia.getKey())).thenReturn(Optional.of(savedMedia));
            return savedMedia;
        }).when(mediaRepository).save(any(Media.class));

        MediaResponse response = service.uploadMedia(file);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(mediaRepository).save(any(Media.class));

        assertNotNull(response);
        assertEquals(presignedUrl, response.presignedUrl());
    }

    @Test
    void givenExistingMediaKey_whenDeleteMediaByKey_thenDeletesFromS3AndRepository() {
        String key = "mediaKey";
        Media media = new Media();
        media.setKey(key);
        when(mediaRepository.findByKey(key)).thenReturn(Optional.of(media));

        service.deleteMediaByKey(key);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(mediaRepository).delete(media);
    }

    @Test
    void givenNonExistingMediaKey_whenDeleteMediaByKey_thenThrowsException() {
        when(mediaRepository.findByKey("unknown")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.deleteMediaByKey("unknown"));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void givenMediaKey_whenGetMediaWithPresignedUrl_thenReturnsResponse() {
        String key = "mediaKey";
        Media media = new Media();
        media.setKey(key);
        when(mediaRepository.findByKey(key)).thenReturn(Optional.of(media));
        when(s3PresignedService.generatePresignedUrl(key)).thenReturn("http://example.com/mediaKey");

        MediaResponse response = service.getMediaWithPresignedUrl(key);

        assertEquals(key, response.key());
    }

    @Test
    void givenActionAndKey_whenUpdateMediaStatus_thenStatusUpdated() {
        String key = "mediaKey";
        Media media = new Media();
        media.setKey(key);
        when(mediaRepository.findByKey(key)).thenReturn(Optional.of(media));

        service.updateMediaStatus("APPROVED", key);

        assertEquals(Status.APPROVED, media.getStatus());
        verify(mediaRepository).save(media);
    }

    @Test
    void givenStatus_whenDeleteMedia_thenDeletesMatchingMedia() {
        Media media1 = new Media();
        media1.setKey("key1");
        media1.setStatus(Status.PENDING);

        Media media2 = new Media();
        media2.setKey("key2");
        media2.setStatus(Status.PENDING);

        when(mediaRepository.findAll()).thenReturn(List.of(media1, media2));

        service.deleteMedia(Status.PENDING);

        verify(s3Client, times(2)).deleteObject(any(DeleteObjectRequest.class));

        verify(mediaRepository).deleteAll(List.of(media1, media2));
    }
}
