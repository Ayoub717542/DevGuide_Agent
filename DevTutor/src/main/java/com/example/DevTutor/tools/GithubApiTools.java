package com.example.DevTutor.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class GithubApiTools {

    private static final int MAX_RESULTS = 5;

    public record Endpoint(String path, String summary, List<String> params) {
    }

    private final List<Endpoint> endpoints;

    public GithubApiTools() {
        try (InputStream in = new ClassPathResource("github/github-api-get.json").getInputStream()) {
            endpoints = Arrays.asList(JsonMapper.builder().build().readValue(in, Endpoint[].class));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read github/github-api-get.json", e);
        }
    }

    @Tool(description = """
            Search the GitHub REST API for read-only (GET) endpoints about repos, issues and pull requests.
            Give a few words like "list my issues" or "repository commits".
            Returns up to 5 matching endpoints with their path, summary and parameter names.
            """)
    public List<Endpoint> searchGithubApi(
            @ToolParam(description = "A few words describing what you want from GitHub")
            String query
    ) {
        List<String> words = Arrays.stream(query.toLowerCase(Locale.ROOT).split("\\W+"))
                .filter(word -> word.length() > 1)
                .toList();
        return endpoints.stream()
                .filter(endpoint -> score(endpoint, words) > 0)
                .sorted(Comparator.comparingInt((Endpoint endpoint) -> score(endpoint, words)).reversed())
                .limit(MAX_RESULTS)
                .toList();
    }

    private static int score(Endpoint endpoint, List<String> words) {
        String text = (endpoint.summary() + " " + endpoint.path()).toLowerCase(Locale.ROOT);
        return (int) words.stream().filter(text::contains).count();
    }
}
