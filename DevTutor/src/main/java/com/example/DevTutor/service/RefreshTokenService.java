package com.example.DevTutor.service;

import com.example.DevTutor.entity.RefreshToken;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    public static final Duration LIFETIME = Duration.ofDays(7);
    public static final String COOKIE_NAME = "refresh_token";

    private final RefreshTokenRepository refreshTokenRepository;

    public record Rotated(User user, String token) {
    }

    public String create(User user) {
        String rawToken = EmailVerificationService.randomToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(EmailVerificationService.hash(rawToken));
        refreshToken.setExpiresAt(Instant.now().plus(LIFETIME));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Rotated rotate(String rawToken) {
        RefreshToken saved = rawToken == null ? null
                : refreshTokenRepository.findByTokenHash(EmailVerificationService.hash(rawToken)).orElse(null);
        if (saved == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again");
        }
        refreshTokenRepository.delete(saved);
        if (saved.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again");
        }
        User user = saved.getUser();
        return new Rotated(user, create(user));
    }

    public ResponseCookie cookie(String rawToken) {
        return buildCookie(rawToken, LIFETIME);
    }

    public ResponseCookie clearCookie() {
        return buildCookie("", Duration.ZERO);
    }

    private ResponseCookie buildCookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }

    public void delete(String rawToken) {
        if (rawToken == null) {
            return;
        }
        refreshTokenRepository.findByTokenHash(EmailVerificationService.hash(rawToken))
                .ifPresent(refreshTokenRepository::delete);
    }
}
