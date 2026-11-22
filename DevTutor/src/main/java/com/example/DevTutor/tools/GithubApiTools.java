package com.example.DevTutor.tools;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GithubApiTools {

    private static final int MAX_RESULTS = 5;
    private static final int MAX_PER_PAGE = 10;
    private static final int MAX_ANSWER_CHARS = 8_000;
    private static final String SAFE_VALUE = "[A-Za-z0-9._-]+";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^}]+}");

    public record Endpoint(String path, String summary, List<String> params) {
    }

    private final List<Endpoint> endpoints;
    private final Map<Endpoint, Pattern> pathPatterns = new HashMap<>();
    private final UserRepository userRepository;
    private final String sharedToken;
    private final RestClient github = RestClient.create();

    public GithubApiTools(UserRepository userRepository, @Value("${github.token}") String sharedToken) {
        this.userRepository = userRepository;
        this.sharedToken = sharedToken;
        try (InputStream in = new ClassPathResource("github/github-api-get.json").getInputStream()) {
            endpoints = Arrays.asList(JsonMapper.builder().build().readValue(in, Endpoint[].class));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read github/github-api-get.json", e);
        }
        for (Endpoint endpoint : endpoints) {
            pathPatterns.put(endpoint, pathPattern(endpoint.path()));
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

    @Tool(description = """
            Call one read-only GitHub REST API endpoint found with searchGithubApi.
            Fill in the path placeholders with real values, for example /repos/octocat/hello-world/issues.
            Put query parameters (like state or per_page) in params. Returns GitHub's JSON answer.
            """)
    public Map<String, Object> callGithubApi(
            @ToolParam(description = "The endpoint path with real values, starting with /")
            String path,

            @ToolParam(description = "Query parameters from the endpoint's params list", required = false)
            Map<String, String> params,

            ToolContext toolContext
    ) {
        URI uri;
        try {
            uri = checkedUri(path, params);
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "error", e.getMessage());
        }
        try {
            String body = github.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + tokenFor(toolContext))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2026-03-10")
                    .retrieve()
                    .body(String.class);
            String answer = body == null ? "" : body;
            boolean cut = answer.length() > MAX_ANSWER_CHARS;
            return Map.of(
                    "success", true,
                    "answer", cut ? answer.substring(0, MAX_ANSWER_CHARS) : answer,
                    "cut", cut);
        } catch (RestClientResponseException e) {
            log.warn("GitHub API call failed: {} {}", e.getStatusCode(), uri.getPath());
            return Map.of("success", false, "error", "GitHub answered " + e.getStatusCode().value());
        }
    }

    URI checkedUri(String path, Map<String, String> params) {
        if (path == null || Arrays.asList(path.split("/")).stream().anyMatch(s -> s.equals(".") || s.equals(".."))) {
            throw new IllegalArgumentException("Path is not allowed");
        }
        Endpoint endpoint = endpoints.stream()
                .filter(e -> pathPatterns.get(e).matcher(path).matches())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Path is not in the GitHub API list. Use searchGithubApi first."));

        Map<String, String> query = new HashMap<>(params == null ? Map.of() : params);
        for (String name : query.keySet()) {
            if (!endpoint.params().contains(name)) {
                throw new IllegalArgumentException("Unknown parameter: " + name);
            }
        }
        if (endpoint.params().contains("per_page")) {
            query.put("per_page", String.valueOf(perPage(query.get("per_page"))));
        }

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.github.com")
                .path(path);
        query.forEach(builder::queryParam);
        return builder.build().encode().toUri();
    }

    private String tokenFor(ToolContext toolContext) {
        Object email = toolContext == null ? null : toolContext.getContext().get("userEmail");
        if (email == null) {
            return sharedToken;
        }
        return userRepository.findByEmail(email.toString())
                .map(User::getGithubToken)
                .filter(token -> !token.isBlank())
                .orElse(sharedToken);
    }

    private static int perPage(String value) {
        try {
            return Math.min(Math.max(Integer.parseInt(value), 1), MAX_PER_PAGE);
        } catch (NumberFormatException e) {
            return MAX_PER_PAGE;
        }
    }

    private static Pattern pathPattern(String specPath) {
        StringBuilder regex = new StringBuilder();
        Matcher matcher = PLACEHOLDER.matcher(specPath);
        int last = 0;
        while (matcher.find()) {
            regex.append(Pattern.quote(specPath.substring(last, matcher.start()))).append(SAFE_VALUE);
            last = matcher.end();
        }
        regex.append(Pattern.quote(specPath.substring(last)));
        return Pattern.compile(regex.toString());
    }

    private static int score(Endpoint endpoint, List<String> words) {
        String text = (endpoint.summary() + " " + endpoint.path()).toLowerCase(Locale.ROOT);
        return (int) words.stream().filter(text::contains).count();
    }
}
