package io.mkalugin.gpt.service;

import io.mkalugin.gpt.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты на {@link ChatService}
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callSpec;

    @Mock
    private ChatClient.AdvisorSpec advisorSpec;

    @Mock
    private InputValidationService inputValidationService;

    private ChatMemory chatMemory;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatMemory = MessageWindowChatMemory.builder().maxMessages(20).build();
        chatService = new ChatService(chatClientBuilder, chatMemory, inputValidationService);
    }

    @Test
    @DisplayName("chat() должен отправить сообщение и вернуть ответ с новым conversationId")
    void chat_shouldSendMessageAndReturnResponseWithNewConversationId() {
        String userMessage = "Hello, GPT!";
        String expectedResponse = "Hello! How can I help you?";

        when(chatClientBuilder.defaultAdvisors(any(MessageChatMemoryAdvisor.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(expectedResponse);

        ChatResponse result = chatService.chat(userMessage, null);

        assertThat(result.response()).isEqualTo(expectedResponse);
        assertThat(result.conversationId()).isNotNull();
        verify(requestSpec).user(userMessage);
    }

    @Test
    @DisplayName("chat() должен использовать существующий conversationId")
    void chat_shouldUseExistingConversationId() {
        String userMessage = "Hello, GPT!";
        String expectedResponse = "Hello! How can I help you?";
        String conversationId = "test-conversation-123";

        when(chatClientBuilder.defaultAdvisors(any(MessageChatMemoryAdvisor.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(expectedResponse);

        ChatResponse result = chatService.chat(userMessage, conversationId);

        assertThat(result.response()).isEqualTo(expectedResponse);
        assertThat(result.conversationId()).isEqualTo(conversationId);
        verify(requestSpec).user(userMessage);
    }
}
