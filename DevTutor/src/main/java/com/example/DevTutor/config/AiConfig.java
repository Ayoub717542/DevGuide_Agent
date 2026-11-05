package com.example.DevTutor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.mcp.ToolContextToMcpMetaConverter;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfig {
    @Bean
    public ChatMemory chatMemory(
            JdbcChatMemoryRepository chatMemoryRepository) {
        return  MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ToolContextToMcpMetaConverter toolContextToMcpMetaConverter() {
        return ToolContextToMcpMetaConverter.noOp();
    }

    @Bean
    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        return exception -> {
            log.warn("Tool call failed", exception);
            return """
                {"success": false,
                 "error": "The tool call failed. Try a different query, or answer without it and say the documentation could not be checked."}
                """;
        };
    }
}