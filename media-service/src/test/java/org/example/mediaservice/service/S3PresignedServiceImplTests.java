package org.example.mediaservice.service;

import org.example.mediaservice.service.impl.S3PresignedServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3PresignedServiceImplTests {
    private S3Presigner presigner;
    private S3PresignedServiceImpl service;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        presigner = mock(S3Presigner.class);
        service = new S3PresignedServiceImpl(presigner);

        Field bucketField = S3PresignedServiceImpl.class.getDeclaredField("bucketName");
        bucketField.setAccessible(true);
        bucketField.set(service, "test-bucket");
    }

    @Test
    void givenKey_whenGeneratePresignedUrl_thenReturnsUrl() throws MalformedURLException {
        String key = "test-file.txt";
        String expectedUrl = "https://example.com/test-file.txt";

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(URI.create(expectedUrl).toURL());

        when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        String actualUrl = service.generatePresignedUrl(key);

        assertEquals(expectedUrl, actualUrl);

        verify(presigner).presignGetObject(argThat((GetObjectPresignRequest req) ->
                req.getObjectRequest().bucket().equals("test-bucket") &&
                req.getObjectRequest().key().equals(key) &&
                req.signatureDuration().toHours() == 48
        ));
    }
}
