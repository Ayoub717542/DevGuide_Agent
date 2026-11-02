package com.example.DevTutor.tools;

import com.example.DevTutor.service.TrelloMcpService.PendingAction;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.util.json.JsonParser;

import java.util.Map;
import java.util.UUID;

public record ApprovalToolCallback(ToolCallback tool, String email, Map<String, PendingAction> pending)
        implements ToolCallback {

    @Override
    public ToolDefinition getToolDefinition() {
        return tool.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return tool.getToolMetadata();
    }

    @Override
    public String call(String input) {
        String id = UUID.randomUUID().toString();
        pending.put(id, new PendingAction(id, email, tool.getToolDefinition().name(), input, tool));
        return JsonParser.toJson(Map.of(
                "status", "NOT_DONE_WAITING_FOR_APPROVAL",
                "approvalId", id,
                "message", "This Trello action was NOT done. Tell the student exactly what you want to do "
                        + "and that they must approve it."));
    }

    @Override
    public String call(String input, ToolContext context) {
        return call(input);
    }
}
