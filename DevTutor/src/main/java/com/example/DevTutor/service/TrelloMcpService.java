package com.example.DevTutor.service;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TrelloMcpService {

    private final TrelloOAuthService trelloOAuthService;
    private final Map<String, SyncMcpToolCallbackProvider> userTools = new ConcurrentHashMap<>();
    private final Map<String, McpSyncClient> userClients = new ConcurrentHashMap<>();

    public List<ToolCallback> toolsFor(String email) {
        if (trelloOAuthService.validAccessToken(email).isEmpty()) {
            forget(email);
            return List.of();
        }
        try {
            return List.of(userTools.computeIfAbsent(email, this::connect).getToolCallbacks());
        } catch (RuntimeException e) {
            forget(email);
            return List.of();
        }
    }

    public void forget(String email) {
        userTools.remove(email);
        McpSyncClient client = userClients.remove(email);
        if (client != null) {
            client.closeGracefully();
        }
    }

    private SyncMcpToolCallbackProvider connect(String email) {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder("https://mcp.trello.com")
                .endpoint("/v1")
                .httpRequestCustomizer((request, method, uri, body, context) ->
                        request.setHeader("Authorization", "Bearer "
                                + trelloOAuthService.validAccessToken(email).orElse("")))
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("DevTutor", "1.0"))
                .requestTimeout(Duration.ofSeconds(60))
                .build();
        client.initialize();
        userClients.put(email, client);

        return SyncMcpToolCallbackProvider.builder().mcpClients(client).build();
    }
}
