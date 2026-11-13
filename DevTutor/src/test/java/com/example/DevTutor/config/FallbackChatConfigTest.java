package com.example.DevTutor.config;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.ServerException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FallbackChatConfigTest {

    private final GoogleGenAiChatModel gemini = mock(GoogleGenAiChatModel.class);
    private final OpenAiChatModel groq = mock(OpenAiChatModel.class);
    private final ChatModel chatModel = new FallbackChatConfig().fallbackChatModel(gemini, groq);
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
    void serverErrorUsesGroq() {
        ChatResponse groqAnswer = new ChatResponse(List.of());
        when(gemini.call(any(Prompt.class))).thenThrow(new RuntimeException("Failed to generate content",
                new ServerException(503, "UNAVAILABLE", "overloaded")));
        when(groq.getOptions()).thenReturn(OpenAiChatOptions.builder().model("groq-model").build());
        when(groq.call(any(Prompt.class))).thenReturn(groqAnswer);

        ChatResponse answer = chatModel.call(prompt);

        assertSame(groqAnswer, answer);
        verify(gemini, times(1)).call(any(Prompt.class));
        ArgumentCaptor<Prompt> groqPrompt = ArgumentCaptor.forClass(Prompt.class);
        verify(groq).call(groqPrompt.capture());
        assertEquals("groq-model", groqPrompt.getValue().getOptions().getModel());
    }

    @Test
    void geminiOutOfTokensUsesGroq() {
        ChatResponse groqAnswer = new ChatResponse(List.of());
        when(gemini.call(any(Prompt.class))).thenThrow(new RuntimeException("Failed to generate content",
                new ClientException(429, "RESOURCE_EXHAUSTED", "quota exceeded")));
        when(groq.getOptions()).thenReturn(OpenAiChatOptions.builder().model("groq-model").build());
        when(groq.call(any(Prompt.class))).thenReturn(groqAnswer);

        ChatResponse answer = chatModel.call(prompt);

        assertSame(groqAnswer, answer);
        verify(gemini, times(1)).call(any(Prompt.class));
        ArgumentCaptor<Prompt> groqPrompt = ArgumentCaptor.forClass(Prompt.class);
        verify(groq).call(groqPrompt.capture());
        assertEquals("groq-model", groqPrompt.getValue().getOptions().getModel());
    }

    @Test
    void streamSwitchesModelWhenErrorComesBeforeFirstWord() {
        when(gemini.stream(any(Prompt.class))).thenReturn(Flux.error(serverError()));
        when(groq.getOptions()).thenReturn(OpenAiChatOptions.builder().model("groq-model").build());
        when(groq.stream(any(Prompt.class))).thenReturn(Flux.just(words("Hello")));

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

    @Test
    void groqToolCallAfterGeminiErrorKeepsNextRoundOnGroq() {
        when(gemini.stream(any(Prompt.class))).thenReturn(Flux.error(serverError()));
        when(groq.getOptions()).thenReturn(OpenAiChatOptions.builder().model("groq-model").build());
        AssistantMessage groqToolCall = toolCall("call_abc123", Map.of(
                "id", "chatcmpl-1", "role", "assistant", "index", 0, "finishReason", "TOOL_CALLS",
                "refusal", "", "annotations", List.of(), "reasoningContent", ""));
        when(groq.stream(any(Prompt.class)))
                .thenReturn(Flux.just(new ChatResponse(List.of(new Generation(groqToolCall)))))
                .thenReturn(Flux.just(words("Done")));

        chatModel.stream(prompt).blockLast();
        List<String> answer = chatModel.stream(nextRound(groqToolCall)).map(this::text).collectList().block();

        assertEquals(List.of("Done"), answer);
        verify(gemini, times(1)).stream(any(Prompt.class));
        verify(groq, times(2)).stream(any(Prompt.class));
    }

    @Test
    void geminiToolCallKeepsNextRoundOnGemini() {
        AssistantMessage geminiToolCall = toolCall("", Map.of("candidateIndex", 0, "finishReason", "STOP"));
        when(gemini.stream(any(Prompt.class))).thenReturn(Flux.just(words("Done")));

        List<String> answer = chatModel.stream(nextRound(geminiToolCall)).map(this::text).collectList().block();

        assertEquals(List.of("Done"), answer);
        verify(gemini, times(1)).stream(any(Prompt.class));
        verify(groq, never()).stream(any(Prompt.class));
    }

    private static AssistantMessage toolCall(String id, Map<String, Object> metadata) {
        return AssistantMessage.builder()
                .content("")
                .properties(metadata)
                .toolCalls(List.of(new AssistantMessage.ToolCall(id, "function", "resolve_library_id", "{}")))
                .build();
    }

    private Prompt nextRound(AssistantMessage toolCall) {
        ToolResponseMessage toolResult = ToolResponseMessage.builder()
                .responses(List.of(new ToolResponseMessage.ToolResponse(
                        toolCall.getToolCalls().getFirst().id(), "resolve_library_id", "{\"id\":\"/spring/ai\"}")))
                .build();
        return new Prompt(List.of(new UserMessage("Hi"), toolCall, toolResult), prompt.getOptions());
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
