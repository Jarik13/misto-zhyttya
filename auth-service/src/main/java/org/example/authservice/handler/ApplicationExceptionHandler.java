package org.example.authservice.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.exception.BusinessException;
import org.example.authservice.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static org.example.authservice.dto.error.ErrorCode.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
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

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleBusiness() {
        ErrorResponse body = ErrorResponse.builder()
                .code(ERR_USER_DISABLED.getCode())
                .message(ERR_USER_DISABLED.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleException(BadCredentialsException exception) {
        log.debug(exception.getMessage(), exception);

        final ErrorResponse response = ErrorResponse.builder()
                .message(BAD_CREDENTIALS.getDefaultMessage())
                .code(BAD_CREDENTIALS.getCode())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(EntityNotFoundException exception) {
        log.debug(exception.getMessage(), exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("TBD")
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(UsernameNotFoundException exception) {
        log.debug(exception.getMessage(), exception);

        ErrorResponse response = ErrorResponse.builder()
                .code(USERNAME_NOT_FOUND.getCode())
                .message(USERNAME_NOT_FOUND.getDefaultMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleException(AuthorizationDeniedException exception) {
        log.debug(exception.getMessage(), exception);

        ErrorResponse response = ErrorResponse.builder()
                .message("You are not authorized to perform this operation")
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
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
