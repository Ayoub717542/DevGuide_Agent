package com.example.DevTutor.service;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrelloOAuthService {

    private static final String AUTHORIZE_URL = "https://auth.atlassian.com/authorize";
    private static final String TOKEN_URL = "https://auth.atlassian.com/oauth/token";
    private static final String REVOKE_URL = "https://auth.atlassian.com/oauth/revoke";
    private static final String MCP_RESOURCE = "https://mcp.trello.com/v1";
    private static final String SCOPES = "offline_access read:me read:account"
            + " read:board:trello write:board:trello"
            + " read:organization:trello write:organization:trello"
            + " read:member:trello"
            + " read:member.inbox:trello write:member.inbox:trello"
            + " read:member.planner:trello write:member.planner:trello";
    private static final Duration LOGIN_TIMEOUT = Duration.ofMinutes(10);

    private record PendingLogin(String email, String verifier, Instant createdAt) {
    }

    private final UserRepository userRepository;
    private final String clientId;
    private final String redirectUri;
    private final RestClient restClient = RestClient.create();
    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingLogin> pendingLogins = new ConcurrentHashMap<>();

    public TrelloOAuthService(UserRepository userRepository,
                              @Value("${app.trello.client-id}") String clientId,
                              @Value("${app.trello.redirect-uri}") String redirectUri) {
        this.userRepository = userRepository;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public String authorizeUrl(String email) {
        pendingLogins.values().removeIf(this::isExpired);

        String state = randomString();
        String verifier = randomString();
        pendingLogins.put(state, new PendingLogin(email, verifier, Instant.now()));

        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", SCOPES)
                .queryParam("state", state)
                .queryParam("code_challenge", sha256(verifier))
                .queryParam("code_challenge_method", "S256")
                .queryParam("resource", MCP_RESOURCE)
                .queryParam("prompt", "consent")
                .encode()
                .toUriString();
    }

    public boolean finishConnect(String code, String state, String email) {
        PendingLogin login = state == null ? null : pendingLogins.remove(state);
        if (login == null || isExpired(login) || code == null || !login.email().equals(email)) {
            return false;
        }

        Map<String, Object> tokens;
        try {
            tokens = postToken(Map.of(
                    "grant_type", "authorization_code",
                    "client_id", clientId,
                    "code", code,
                    "redirect_uri", redirectUri,
                    "code_verifier", login.verifier()));
        } catch (RestClientException e) {
            return false;
        }
        if (tokens == null || tokens.get("access_token") == null) {
            return false;
        }

        User user = userRepository.findByEmail(login.email()).orElse(null);
        if (user == null) {
            return false;
        }
        saveTokens(user, tokens);
        return true;
    }

    public synchronized Optional<String> validAccessToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getTrelloAccessToken() == null) {
            return Optional.empty();
        }
        Instant expiresAt = user.getTrelloTokenExpiresAt();
        if (expiresAt != null && expiresAt.isAfter(Instant.now().plusSeconds(60))) {
            return Optional.of(user.getTrelloAccessToken());
        }
        if (user.getTrelloRefreshToken() == null) {
            return Optional.empty();
        }

        Map<String, Object> tokens;
        try {
            tokens = postToken(Map.of(
                    "grant_type", "refresh_token",
                    "client_id", clientId,
                    "refresh_token", user.getTrelloRefreshToken()));
        } catch (HttpClientErrorException e) {
            clearTokens(user);
            return Optional.empty();
        } catch (RestClientException e) {
            return Optional.empty();
        }
        if (tokens == null || tokens.get("access_token") == null) {
            return Optional.empty();
        }
        saveTokens(user, tokens);
        return Optional.of(user.getTrelloAccessToken());
    }

    public void disconnect(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            revoke(user.getTrelloRefreshToken());
            clearTokens(user);
        });
    }

    private Map<String, Object> postToken(Map<String, String> body) {
        return restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private void saveTokens(User user, Map<String, Object> tokens) {
        long expiresIn = ((Number) tokens.getOrDefault("expires_in", 3600)).longValue();
        user.setTrelloAccessToken((String) tokens.get("access_token"));
        user.setTrelloRefreshToken((String) tokens.get("refresh_token"));
        user.setTrelloTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
        userRepository.save(user);
    }

    private void clearTokens(User user) {
        user.setTrelloAccessToken(null);
        user.setTrelloRefreshToken(null);
        user.setTrelloTokenExpiresAt(null);
        userRepository.save(user);
    }

    private void revoke(String refreshToken) {
        if (refreshToken == null) {
            return;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        form.add("client_id", clientId);
        try {
            restClient.post()
                    .uri(REVOKE_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ignored) {
        }
    }

    private boolean isExpired(PendingLogin login) {
        return login.createdAt().plus(LOGIN_TIMEOUT).isBefore(Instant.now());
    }

    private String randomString() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
