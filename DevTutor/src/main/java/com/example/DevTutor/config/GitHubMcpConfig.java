package com.example.DevTutor.config;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.common.McpTransportContext;

import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.net.http.HttpRequest;
import java.util.Map;

@Configuration
public class GitHubMcpConfig {

    @Bean
    public McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>
    githubAuthentication(
            @Value("${github.token}") String token
    ) {

        return (name, builder) -> {
            if ("github".equals(name)) {
                builder.requestBuilder(
                                HttpRequest.newBuilder()
                                        .header("X-MCP-Readonly", "true")
                                        .header("X-MCP-Toolsets", "repos,issues,pull_requests,context"))
                        .httpRequestCustomizer((request, method, uri, body, context) -> {
                            Object userToken = context == null ? null : context.get("githubToken");
                            request.setHeader("Authorization",
                                    "Bearer " + (userToken != null ? userToken : token));
                            if (userToken == null && body != null && body.contains("\"tools/call\"")) {
                                request.setHeader("X-MCP-Toolsets", "repos,issues,pull_requests");
                            }
                        });
            }
        };
    }

    @Bean
    public McpClientCustomizer<McpClient.SyncSpec> githubUserToken(UserRepository userRepository) {
        return (name, spec) -> {
            if ("github".equals(name)) {
                spec.transportContextProvider(() -> currentUserToken(userRepository));
            }
        };
    }

    private static McpTransportContext currentUserToken(UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return McpTransportContext.EMPTY;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getGithubToken)
                .map(token -> McpTransportContext.create(Map.of("githubToken", token)))
                .orElse(McpTransportContext.EMPTY);
    }
}