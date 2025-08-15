package org.example.userprofileservice.dto.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Ресурс не знайдено", HttpStatus.NOT_FOUND),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Внутрішня помилка сервера, будь ласка, спробуйте пізніше або зверніться до адміністратора", HttpStatus.INTERNAL_SERVER_ERROR),
    PHONE_ALREADY_EXISTS("PHONE_ALREADY_EXISTS", "Цей номер телефону вже зайнятий", HttpStatus.CONFLICT),
    ;

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(final String code,
              final String defaultMessage,
              final HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }
}
