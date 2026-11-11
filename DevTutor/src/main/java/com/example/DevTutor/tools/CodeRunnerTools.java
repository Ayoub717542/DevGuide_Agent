package com.example.DevTutor.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CodeRunnerTools {

    private final RestClient piston;

    public CodeRunnerTools(@Value("${app.piston.url}") String pistonUrl) {
        this.piston = RestClient.create(pistonUrl);
    }

    @Tool(description = """
            Run code and return its real output.
            Supported languages: java, python, javascript, c, c++.
            Use it to check a code example before showing it, or when the user asks to run code.
            Java code must have a public class with a main method.
            C and C++ code must have a main function.
            Show the user the output you got.
            """)
    public Map<String, Object> runCode(
            @ToolParam(description = "java, python, javascript, c or c++")
            String language,

            @ToolParam(description = "The complete source code to run")
            String code
    ) {
        try {
            Map<?, ?> response = piston.post()
                    .uri("/api/v2/execute")
                    .body(Map.of(
                            "language", language.toLowerCase(),
                            "version", "*",
                            "files", List.of(Map.of("content", code))))
                    .retrieve()
                    .body(Map.class);

            Map<?, ?> run = (Map<?, ?>) response.get("run");
            Object message = run.get("message");

            return Map.of(
                    "success", message == null,
                    "output", String.valueOf(run.get("output")),
                    "message", message == null ? "" : message.toString()
            );

        } catch (RuntimeException e) {

            log.error("Code execution failed", e);

            return Map.of(
                    "success", false,
                    "error", "The code could not be run. Answer without running it and say so."
            );
        }
    }
}
