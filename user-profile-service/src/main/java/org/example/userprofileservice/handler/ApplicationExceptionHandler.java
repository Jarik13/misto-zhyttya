package org.example.userprofileservice.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.userprofileservice.dto.error.ErrorResponse;
import org.example.userprofileservice.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static org.example.userprofileservice.dto.error.ErrorCode.INTERNAL_EXCEPTION;
import static org.example.userprofileservice.dto.error.ErrorCode.RESOURCE_NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .code(ex.getErrorCode()
                        .getCode())
                .message(ex.getMessage())
                .build();

        log.info("Business Exception: {}", body);
        log.debug(ex.getMessage(), ex);

        return ResponseEntity.status(ex.getErrorCode().getStatus() != null ?
                ex.getErrorCode().getStatus() :
                HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exp) {
        List<ErrorResponse.ValidationError> errors = new ArrayList<>();

        exp.getBindingResult().getFieldErrors().forEach(error -> errors
                .add(ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .code(error.getCode())
                        .message(error.getDefaultMessage())
                        .build()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("Validation failed for one or more fields")
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
