package io.mkalugin.gpt.controller;

import io.mkalugin.gpt.dto.ChatRequest;
import io.mkalugin.gpt.dto.ChatResponse;
import io.mkalugin.gpt.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для прямого общения с GPT моделью.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API для прямого общения с GPT моделью")
public class ChatController {

    private final ChatService chatService;

    /**
     * Отправление сообщения в GPT и получение ответа.
     * Поддерживает память разговора через conversationId.
     *
     * @param request запрос с сообщением пользователя и опциональным conversationId
     * @return ответ от модели с conversationId для продолжения диалога
     */
    @Operation(
            summary = "Отправить сообщение в чат",
            description = "Отправляет сообщение в GPT и получает ответ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный ответ от модели"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (пустое сообщение или превышен лимит символов)")
    })
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request.message(), request.conversationId());
    }
}
