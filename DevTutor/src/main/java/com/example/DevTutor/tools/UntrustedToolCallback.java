package com.example.DevTutor.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.Arrays;
import java.util.List;

public record UntrustedToolCallback(ToolCallback tool) implements ToolCallback {

    public static List<ToolCallback> wrap(List<ToolCallback> tools) {
        return tools.stream().<ToolCallback>map(UntrustedToolCallback::new).toList();
    }

    public static ToolCallbackProvider wrap(ToolCallbackProvider provider) {
        return () -> wrap(Arrays.asList(provider.getToolCallbacks())).toArray(ToolCallback[]::new);
    }

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
        return box(tool.call(input));
    }

    @Override
    public String call(String input, ToolContext context) {
        return box(tool.call(input, context));
    }

    public static String clean(String text) {
        return text.replaceAll("(?i)<\\s*/?\\s*untrusted_(tool_output|documents)\\s*>", "");
    }

    private String box(String result) {
        return "<untrusted_tool_output>\n" + clean(result) + "\n</untrusted_tool_output>";
    }
}
