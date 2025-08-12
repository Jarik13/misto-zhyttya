package org.example.mediaservice.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.mediaservice.dto.error.ErrorResponse;
import org.example.mediaservice.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.example.mediaservice.dto.error.ErrorCode.INTERNAL_EXCEPTION;
import static org.example.mediaservice.dto.error.ErrorCode.RESOURCE_NOT_FOUND;


@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        log.warn("Business exception: {}", exception.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(exception.getErrorCode().getCode())
                .message(exception.getMessage())
                .build();

        HttpStatus status = exception.getErrorCode().getStatus() != null
                ? exception.getErrorCode().getStatus()
                : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        log.warn("Upload failed: file size exceeded the maximum limit");

        String message = String.format("Розмір файлу перевищує дозволений ліміт: %s", maxFileSize);

        ErrorResponse response = ErrorResponse.builder()
                .code("FILE_TOO_LARGE")
                .message(message)
                .build();

        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
        log.warn("Entity not found: {}", exception.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(RESOURCE_NOT_FOUND.getCode())
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);

        ErrorResponse response = ErrorResponse.builder()
                .code(INTERNAL_EXCEPTION.getCode())
                .message(INTERNAL_EXCEPTION.getDefaultMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
