package io.mkalugin.gpt.enums;

/**
 * Коды ошибок для API.
 */
public enum ErrorCode {
    BAD_REQUEST("Bad Request"),
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    VALIDATION_ERROR("Validation Error");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
