package com.example.DevTutor.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    void chatMemoryKeepsOnlyTheLastTenMessages() {
        JdbcChatMemoryRepository repository = mock(JdbcChatMemoryRepository.class);
        List<Message> stored = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            stored.add(i % 2 == 1 ? new UserMessage("question " + i) : new AssistantMessage("answer " + i));
        }
        when(repository.findByConversationId("7")).thenReturn(stored);
        ChatMemory chatMemory = new AiConfig().chatMemory(repository);

        chatMemory.add("7", new AssistantMessage("answer 16"));

        ArgumentCaptor<List<Message>> saved = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(eq("7"), saved.capture());
        assertEquals(10, saved.getValue().size());
        assertEquals("question 7", saved.getValue().getFirst().getText());
        assertEquals("answer 16", saved.getValue().getLast().getText());
    }
}
