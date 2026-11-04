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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.example.DevTutor.util.Exceptions.findCause;

@Configuration
public class FallbackChatConfig {

    private static final Logger log = LoggerFactory.getLogger(FallbackChatConfig.class);

    @Bean
    @Primary
    public ChatModel fallbackChatModel(
            GoogleGenAiChatModel gemini,
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
                    ChatOptions options = prompt.getOptions() != null
                            ? prompt.getOptions()
                            : gemini.getOptions();
                    return gemini.call(prompt.mutate()
                            .chatOptions(options.mutate().model(fallbackModel).build())
                            .build());
                }
            }

            @Override
            public ChatOptions getOptions() {
                return gemini.getOptions();
            }
        };
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
