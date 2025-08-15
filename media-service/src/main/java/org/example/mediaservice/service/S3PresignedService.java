package org.example.mediaservice.service;

public interface S3PresignedService {
    String generatePresignedUrl(String key);
}
