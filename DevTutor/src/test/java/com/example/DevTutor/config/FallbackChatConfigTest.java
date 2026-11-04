package com.example.DevTutor.config;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.ServerException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FallbackChatConfigTest {

    private final GoogleGenAiChatModel gemini = mock(GoogleGenAiChatModel.class);
    private final ChatModel chatModel = new FallbackChatConfig().fallbackChatModel(gemini, "backup-model");
    private final Prompt prompt = new Prompt("Hi", ChatOptions.builder().model("main-model").build());

    @Test
    void configErrorIsNotHiddenByBackup() {
        RuntimeException error = new RuntimeException("Failed to generate content",
                new ClientException(404, "NOT_FOUND", "model not found"));
        when(gemini.call(any(Prompt.class))).thenThrow(error);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> chatModel.call(prompt));

        assertSame(error, thrown);
        verify(gemini, times(1)).call(any(Prompt.class));
    }

    @Test
    void serverErrorUsesBackupModel() {
        ChatResponse backupAnswer = new ChatResponse(List.of());
        when(gemini.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("Failed to generate content",
                        new ServerException(503, "UNAVAILABLE", "overloaded")))
                .thenReturn(backupAnswer);

        ChatResponse answer = chatModel.call(prompt);

        assertSame(backupAnswer, answer);
        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(gemini, times(2)).call(prompts.capture());
        assertEquals("backup-model", prompts.getAllValues().get(1).getOptions().getModel());
    }
}
