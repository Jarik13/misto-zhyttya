package org.example.mediaservice.exception;

import lombok.Getter;
import org.example.mediaservice.dto.error.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(getFormatterMessage(errorCode, args));
        this.errorCode = errorCode;
        this.args = args;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.args = new Object[]{};
    }

    private static String getFormatterMessage(ErrorCode errorCode, Object... args) {
        if (args != null && args.length > 0) {
            return String.format(errorCode.getDefaultMessage(), args);
        }
        return errorCode.getDefaultMessage();
    }
}
