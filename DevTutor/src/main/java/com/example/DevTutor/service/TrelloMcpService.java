package com.example.DevTutor.service;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import com.example.DevTutor.tools.ApprovalToolCallback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TrelloMcpService {

    private final TrelloOAuthService trelloOAuthService;
    private final Map<String, SyncMcpToolCallbackProvider> userTools = new ConcurrentHashMap<>();
    private final Map<String, McpSyncClient> userClients = new ConcurrentHashMap<>();
    private final Map<String, PendingAction> pending = new ConcurrentHashMap<>();

    public record PendingAction(String id, @JsonIgnore String email, String toolName, String input,
                                @JsonIgnore ToolCallback tool) {
    }

    public List<PendingAction> pendingFor(String email) {
        return pending.values().stream().filter(action -> action.email().equals(email)).toList();
    }

    public String approve(String id, String email) {
        PendingAction action = take(id, email);
        return action.tool().call(action.input());
    }

    public void reject(String id, String email) {
        take(id, email);
    }

    private PendingAction take(String id, String email) {
        PendingAction action = pending.get(id);
        if (action == null || !action.email().equals(email) || !pending.remove(id, action)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pending action not found");
        }
        return action;
    }

    public List<ToolCallback> toolsFor(String email) {
        if (trelloOAuthService.validAccessToken(email).isEmpty()) {
            forget(email);
            return List.of();
        }
        try {
            return Arrays.stream(userTools.computeIfAbsent(email, this::connect).getToolCallbacks())
                    .map(tool -> isRead(tool) ? tool : new ApprovalToolCallback(tool, email, pending))
                    .toList();
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

    private boolean isRead(ToolCallback tool) {
        String name = tool.getToolDefinition().name();
        return name.startsWith("trelloRead") || name.equals("trelloSearch");
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
