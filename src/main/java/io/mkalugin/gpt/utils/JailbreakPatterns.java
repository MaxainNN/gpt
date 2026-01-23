package io.mkalugin.gpt.utils;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Паттерны для обнаружения попыток jailbreak.
 * Включают распространённые техники обхода инструкций.
 */
@UtilityClass
public class JailbreakPatterns {

    public final List<Pattern> PATTERNS = List.of(
            // Прямые попытки игнорировать инструкции
            Pattern.compile("(?i)(ignore|forget|disregard).{0,20}(previous|above|all).{0,20}(instructions?|rules?|prompts?)"),
            Pattern.compile("(?i)(ignore|forget|disregard).{0,20}(system|assistant).{0,20}(prompt|message|instructions?)"),

            // DAN и подобные jailbreak
            Pattern.compile("(?i)\\bDAN\\b.{0,30}(mode|jailbreak|anything)"),
            Pattern.compile("(?i)(you are|act as|pretend).{0,20}(DAN|jailbroken|unrestricted|unfiltered)"),

            // Попытки переопределить роль
            Pattern.compile("(?i)(new|your).{0,10}(instruction|rule|role).{0,10}(is|are|:)"),
            Pattern.compile("(?i)(from now on|starting now).{0,30}(you|ignore|forget)"),

            // Developer mode / режим разработчика
            Pattern.compile("(?i)(developer|dev|admin|sudo|root).{0,10}(mode|access|override)"),
            Pattern.compile("(?i)(enable|activate|enter).{0,20}(jailbreak|unrestricted|developer)"),

            // Попытки через ролевую игру
            Pattern.compile("(?i)(roleplay|pretend|imagine).{0,30}(no.{0,10}(rules|restrictions|limits))"),

            // Base64/encoded попытки
            Pattern.compile("(?i)(decode|base64|execute).{0,20}(following|this|instruction)"),

            // Попытки через "гипотетические" сценарии
            Pattern.compile("(?i)(hypothetically|theoretically|in theory).{0,30}(if you (could|were|had))"),

            // Prompt injection через разделители
            Pattern.compile("(?i)(\\[SYSTEM\\]|\\[ADMIN\\]|\\[OVERRIDE\\]|###\\s*SYSTEM)"),

            // Русскоязычные паттерны
            Pattern.compile("(?i)(игнорируй|забудь|отмени).{0,20}(предыдущие|все|системные).{0,20}(инструкции|правила|указания)"),
            Pattern.compile("(?i)(ты теперь|отныне ты|представь что ты).{0,30}(без ограничений|можешь всё|не имеешь правил)")
    );
}
