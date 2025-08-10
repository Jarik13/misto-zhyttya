package org.example.authservice.dto.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS("ERR_EMAIL_EXISTS", "Електронна пошта вже існує", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS("ERR_PHONE_EXISTS", "Обліковий запис з цим номером телефону вже існує", HttpStatus.CONFLICT),
    PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "Пароль та підтвердження не співпадають", HttpStatus.BAD_REQUEST),
    CHANGE_PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "Новий пароль та підтвердження не співпадають", HttpStatus.BAD_REQUEST),
    ERR_SENDING_ACTIVATION_EMAIL("ERR_SENDING_ACTIVATION_EMAIL",
            "Сталася помилка під час відправки листа активації",
            HttpStatus.INTERNAL_SERVER_ERROR),

    ERR_USER_DISABLED("ERR_USER_DISABLED",
            "Обліковий запис користувача вимкнено, будь ласка, активуйте акаунт або зверніться до адміністратора",
            HttpStatus.UNAUTHORIZED),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "Поточний пароль неправильний", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER_NOT_FOUND", "Користувача не знайдено", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_DEACTIVATED("ACCOUNT_ALREADY_DEACTIVATED", "Акаунт вже деактивовано", HttpStatus.BAD_REQUEST),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Неправильне ім'я користувача та/або пароль", HttpStatus.UNAUTHORIZED),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION",
            "Сталася внутрішня помилка, будь ласка, спробуйте ще раз або зверніться до адміністратора",
            HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "Користувача з вказаним ім'ям не знайдено", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS_FOR_USER("CATEGORY_ALREADY_EXISTS_FOR_USER", "Категорія вже існує для цього користувача", HttpStatus.CONFLICT),
    INVALID_TOKEN("INVALID_TOKEN", "Недійсний токен", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Недійсний токен оновлення", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "Доступ заборонено. Будь ласка, увійдіть у систему.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_MISSED("REFRESH_TOKEN_MISSED", "Токен оновлення відсутній у куках запиту", HttpStatus.UNAUTHORIZED),
    PHONE_IS_EMPTY("PHONE_IS_EMPTY", "Номер телефону порожній", HttpStatus.BAD_REQUEST),
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
