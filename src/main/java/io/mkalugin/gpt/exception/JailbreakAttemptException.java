package io.mkalugin.gpt.exception;

/**
 * Исключение, выбрасываемое при обнаружении попытки jailbreak.
 */
public class JailbreakAttemptException extends RuntimeException {

    public JailbreakAttemptException(String message) {
        super(message);
    }
}
