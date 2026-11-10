package com.example.DevTutor.config;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.example.DevTutor.util.Exceptions.findCause;

@Configuration
public class FallbackChatConfig {

    private static final Logger log = LoggerFactory.getLogger(FallbackChatConfig.class);

    @Bean
    @Primary
    public ChatModel fallbackChatModel(
            GoogleGenAiChatModel gemini,
            OpenAiChatModel groq,
            @Value("${app.ai.fallback-model}") String fallbackModel) {

        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                try {
                    return gemini.call(prompt);
                } catch (RuntimeException e) {
                    if (!isRecoverable(e)) {
                        throw e;
                    }
                    log.warn("Main model failed ({}), using fallback {}", e.getMessage(), fallbackModel);
                }
                ChatOptions options = prompt.getOptions() != null
                        ? prompt.getOptions()
                        : gemini.getOptions();
                try {
                    return gemini.call(prompt.mutate()
                            .chatOptions(options.mutate().model(fallbackModel).build())
                            .build());
                } catch (RuntimeException e) {
                    if (!isRecoverable(e)) {
                        throw e;
                    }
                    String groqModel = groq.getOptions().getModel();
                    log.warn("Backup Gemini failed ({}), using Groq {}", e.getMessage(), groqModel);
                    return groq.call(prompt.mutate()
                            .chatOptions(groqOptions(options, groqModel))
                            .build());
                }
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                ChatOptions options = prompt.getOptions() != null ? prompt.getOptions() : gemini.getOptions();
                return gemini.stream(prompt)
                        .onErrorResume(e -> e instanceof RuntimeException r && isRecoverable(r),
                                e -> groq.stream(prompt.mutate()
                                        .chatOptions(groqOptions(options, groq.getOptions().getModel()))
                                        .build()));
            }

            @Override
            public ChatOptions getOptions() {
                return gemini.getOptions();
            }
        };
    }

    private static OpenAiChatOptions groqOptions(ChatOptions options, String model) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder()
                .model(model)
                .extraBody(Map.of("include_reasoning", false));
        if (options instanceof ToolCallingChatOptions tools) {
            builder.toolCallbacks(tools.getToolCallbacks()).toolContext(tools.getToolContext());
        }
        return builder.build();
    }

    private static boolean isRecoverable(RuntimeException e) {
        if (findCause(e, ServerException.class).isPresent()
                || findCause(e, GenAiIOException.class).isPresent()) {
            return true;
        }
        return findCause(e, ClientException.class)
                .map(client -> client.code() == 429 || client.code() == 408)
                .orElse(false);
    }
}
