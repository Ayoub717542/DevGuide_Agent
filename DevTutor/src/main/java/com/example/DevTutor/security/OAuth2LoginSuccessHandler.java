package com.example.DevTutor.security;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    @Value("${app.oauth2.redirect-url:http://localhost:5173/oauth/callback}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String provider = oauth.getAuthorizedClientRegistrationId();
        OAuth2User profile = oauth.getPrincipal();

        String accessToken = authorizedClientService
                .loadAuthorizedClient(provider, oauth.getName())
                .getAccessToken()
                .getTokenValue();

        String email = switch (provider) {
            case "google" -> Boolean.TRUE.equals(profile.getAttribute("email_verified"))
                    ? profile.getAttribute("email") : null;
            case "github" -> verifiedGitHubEmail(accessToken, profile.getAttribute("email"));
            default -> null;
        };
        if (email == null) {
            response.sendRedirect(redirectUrl + "#error=no_email");
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(normalizedEmail);
            newUser.setName(displayName(profile));
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setEmailVerified(true);
            return newUser;
        });
        if (!user.isEmailVerified()) {
            emailVerificationService.resend(normalizedEmail, request.getRemoteAddr());
            response.sendRedirect(redirectUrl + "#error=verify_email_first");
            return;
        }

        if ("github".equals(provider)) {
            user.setGithubToken(accessToken);
        }
        userRepository.save(user);

        response.sendRedirect(redirectUrl + "#token=" + jwtService.generateToken(user));
    }

    private static String displayName(OAuth2User profile) {
        String name = profile.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = profile.getAttribute("login");
        }
        return name == null ? "New user" : name;
    }

    private static String verifiedGitHubEmail(String accessToken, String profileEmail) {
        List<Map<String, Object>> emails = RestClient.create("https://api.github.com")
                .get()
                .uri("/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(List.class);

        if (emails == null) {
            return null;
        }
        List<Map<String, Object>> verified = emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                .toList();
        if (profileEmail != null && verified.stream().anyMatch(e -> profileEmail.equalsIgnoreCase((String) e.get("email")))) {
            return profileEmail;
        }
        return verified.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElse(null);
    }
}
