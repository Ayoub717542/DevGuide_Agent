package com.example.DevTutor.config;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.NestedExceptionUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.example.DevTutor.util.Exceptions.findCause;

@Configuration
public class FallbackChatConfig {

    private static final Logger log = LoggerFactory.getLogger(FallbackChatConfig.class);
    private static final String GEMINI_MESSAGE_KEY = "candidateIndex";

    @Bean
    @Primary
    public ChatModel fallbackChatModel(
            GoogleGenAiChatModel gemini,
            OpenAiChatModel groq) {

        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                ChatOptions options = prompt.getOptions() != null ? prompt.getOptions() : gemini.getOptions();
                if (toolCallFromAnotherModel(prompt)) {
                    log.info("Answer started on Groq, continuing on Groq");
                    return groq.call(groqPrompt(prompt, options, groq.getOptions().getModel()));
                }
                try {
                    return gemini.call(prompt);
                } catch (RuntimeException e) {
                    if (!isRecoverable(e)) {
                        throw e;
                    }
                    String groqModel = groq.getOptions().getModel();
                    log.warn("Main model failed ({}), using Groq {}",
                            NestedExceptionUtils.getMostSpecificCause(e).toString(), groqModel);
                    return groq.call(groqPrompt(prompt, options, groqModel));
                }
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                ChatOptions options = prompt.getOptions() != null ? prompt.getOptions() : gemini.getOptions();
                if (toolCallFromAnotherModel(prompt)) {
                    log.info("Answer started on Groq, continuing on Groq");
                    return groq.stream(groqPrompt(prompt, options, groq.getOptions().getModel()));
                }
                return beforeFirstWord("Main model", () -> gemini.stream(prompt),
                        () -> groq.stream(groqPrompt(prompt, options, groq.getOptions().getModel())));
            }

            @Override
            public ChatOptions getOptions() {
                return gemini.getOptions();
            }
        };
    }

    private static Flux<ChatResponse> beforeFirstWord(String name, Supplier<Flux<ChatResponse>> model,
                                                      Supplier<Flux<ChatResponse>> backup) {
        return Flux.defer(() -> {
            AtomicBoolean started = new AtomicBoolean();
            return Flux.defer(model)
                    .doOnNext(response -> {
                        if (hasText(response)) {
                            started.set(true);
                        }
                    })
                    .onErrorResume(e -> !started.get() && e instanceof RuntimeException r && isRecoverable(r),
                            e -> {
                                log.warn("{} failed before answering ({}), switching model", name,
                                        NestedExceptionUtils.getMostSpecificCause(e).toString());
                                return Flux.defer(backup);
                            });
        });
    }

    private static boolean hasText(ChatResponse response) {
        return response.getResult() != null
                && response.getResult().getOutput().getText() != null
                && !response.getResult().getOutput().getText().isEmpty();
    }

    static boolean toolCallFromAnotherModel(Prompt prompt) {
        return prompt.getInstructions().stream()
                .anyMatch(message -> message instanceof AssistantMessage assistant
                        && assistant.hasToolCalls()
                        && !assistant.getMetadata().containsKey(GEMINI_MESSAGE_KEY));
    }

    private static Prompt groqPrompt(Prompt prompt, ChatOptions options, String groqModel) {
        return prompt.mutate()
                .messages(withoutImages(prompt))
                .chatOptions(groqOptions(options, groqModel))
                .build();
    }

    private static List<Message> withoutImages(Prompt prompt) {
        return prompt.getInstructions().stream()
                .map(message -> {
                    if (message instanceof UserMessage user && !user.getMedia().isEmpty()) {
                        return (Message) user.mutate()
                                .media(List.of())
                                .text(user.getText() + "\n\n(The student attached an image you cannot see. Say so and answer from the text.)")
                                .build();
                    }
                    return message;
                })
                .toList();
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
