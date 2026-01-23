package io.mkalugin.gpt.exception;

import io.mkalugin.gpt.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * Глобальный обработчик исключений.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка исключения {@link MethodArgumentNotValidException}.
     * Ошибка валидации входных данных.
     *
     * @param ex исключение
     * @return ErrorResponse с данными об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        return ErrorResponse.builder()
                .message("Ошибка валидации")
                .error(ErrorCode.VALIDATION_ERROR.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .details(ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .toList())
                .build();
    }

    /**
     * Обработка исключения {@link JailbreakAttemptException}.
     * Попытка jailbreak атаки.
     *
     * @param ex исключение
     * @return ErrorResponse с данными об ошибке
     */
    @ExceptionHandler(JailbreakAttemptException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleJailbreakException(JailbreakAttemptException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .error(ErrorCode.BAD_REQUEST.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    /**
     * Обработка исключения {@link IllegalArgumentException}.
     * Некорректные аргументы.
     *
     * @param ex исключение
     * @return ErrorResponse с данными об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .error(ErrorCode.BAD_REQUEST.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    /**
     * Обработка исключения {@link IOException}.
     * Ошибка при работе с файлами.
     *
     * @param ex исключение
     * @return ErrorResponse с данными об ошибке
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleIOException(IOException ex) {
        return ErrorResponse.builder()
                .message("Ошибка при работе с файлами: " + ex.getMessage())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

    /**
     * Обработка исключения {@link Exception}.
     * Базовое исключение.
     *
     * @param ex исключение
     * @return ErrorResponse с данными об ошибке
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }
}
