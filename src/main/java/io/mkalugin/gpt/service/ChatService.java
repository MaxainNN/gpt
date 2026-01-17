package io.mkalugin.gpt.service;

import io.mkalugin.gpt.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для прямого взаимодействия с GPT моделью.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;

    /**
     * Отправление сообщения пользователя в GPT модель и возврат ответа.
     * Поддерживает память разговора через conversationId.
     *
     * @param userMessage текст сообщения от пользователя
     * @param conversationId идентификатор разговора (если null - создаётся новый)
     * @return ответ от модели с conversationId
     */
    public ChatResponse chat(String userMessage, String conversationId) {
        String convId = conversationId != null ? conversationId : UUID.randomUUID().toString();

        String response = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build()
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, convId))
                .call()
                .content();

        return new ChatResponse(response, convId);
    }
}
