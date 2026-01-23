package io.mkalugin.gpt.service;

import io.mkalugin.gpt.exception.JailbreakAttemptException;
import io.mkalugin.gpt.utils.JailbreakPatterns;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Сервис для валидации пользовательского ввода.
 * Защищает от jailbreak атак и проверяет длину сообщений.
 */
@Service
public class InputValidationService {

    @Value("${app.moderation.jailbreak-protection:true}")
    private boolean jailbreakProtectionEnabled;

    @Value("${app.moderation.max-input-length:10000}")
    private int maxInputLength;

    /**
     * Валидирует пользовательский ввод.
     *
     * @param input текст для проверки
     * @throws JailbreakAttemptException если обнаружена попытка jailbreak
     * @throws IllegalArgumentException если ввод слишком длинный
     */
    public void validate(String input) {
        if (input == null || input.isBlank()) {
            return;
        }

        // Проверка длины
        if (input.length() > maxInputLength) {
            throw new IllegalArgumentException(
                    "Input too long. Maximum allowed: " + maxInputLength + " characters");
        }

        // Проверка jailbreak паттернов
        if (jailbreakProtectionEnabled) {
            checkForJailbreak(input);
        }
    }

    /**
     * Проверяет текст на наличие jailbreak паттернов.
     */
    private void checkForJailbreak(String input) {
        for (Pattern pattern : JailbreakPatterns.PATTERNS) {
            if (pattern.matcher(input).find()) {
                throw new JailbreakAttemptException(
                        "Potential jailbreak attempt detected. This incident has been logged.");
            }
        }
    }

    /**
     * Проверяет, содержит ли текст подозрительные паттерны (без выброса исключения).
     *
     * @param input текст для проверки
     * @return true если обнаружены подозрительные паттерны
     */
    public boolean containsSuspiciousPatterns(String input) {
        if (input == null || input.isBlank() || !jailbreakProtectionEnabled) {
            return false;
        }

        for (Pattern pattern : JailbreakPatterns.PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }
}
