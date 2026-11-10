package com.example.DevTutor.config;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.ServerException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

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
    private final OpenAiChatModel groq = mock(OpenAiChatModel.class);
    private final ChatModel chatModel = new FallbackChatConfig().fallbackChatModel(gemini, groq, "backup-model");
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

    @Test
    void bothGeminiOutOfTokensUsesGroq() {
        ChatResponse groqAnswer = new ChatResponse(List.of());
        when(gemini.call(any(Prompt.class))).thenThrow(new RuntimeException("Failed to generate content",
                new ClientException(429, "RESOURCE_EXHAUSTED", "quota exceeded")));
        when(groq.getOptions()).thenReturn(OpenAiChatOptions.builder().model("groq-model").build());
        when(groq.call(any(Prompt.class))).thenReturn(groqAnswer);

        ChatResponse answer = chatModel.call(prompt);

        assertSame(groqAnswer, answer);
        verify(gemini, times(2)).call(any(Prompt.class));
        ArgumentCaptor<Prompt> groqPrompt = ArgumentCaptor.forClass(Prompt.class);
        verify(groq).call(groqPrompt.capture());
        assertEquals("groq-model", groqPrompt.getValue().getOptions().getModel());
    }

    @Test
    void streamSwitchesModelWhenErrorComesBeforeFirstWord() {
        when(gemini.stream(any(Prompt.class)))
                .thenReturn(Flux.error(serverError()))
                .thenReturn(Flux.just(words("Hello")));

        List<String> answer = chatModel.stream(prompt).map(this::text).collectList().block();

        assertEquals(List.of("Hello"), answer);
    }

    @Test
    void streamDoesNotSwitchModelAfterFirstWord() {
        when(gemini.stream(any(Prompt.class)))
                .thenReturn(Flux.just(words("Hello")).concatWith(Flux.error(serverError())));

        assertThrows(RuntimeException.class, () -> chatModel.stream(prompt).blockLast());
        verify(gemini, times(1)).stream(any(Prompt.class));
    }

    private static RuntimeException serverError() {
        return new RuntimeException("Failed to generate content",
                new ServerException(503, "UNAVAILABLE", "overloaded"));
    }

    private static ChatResponse words(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    private String text(ChatResponse response) {
        return response.getResult().getOutput().getText();
    }
}
