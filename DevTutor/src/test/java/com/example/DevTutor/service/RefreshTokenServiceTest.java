package com.example.DevTutor.service;

import com.example.DevTutor.entity.RefreshToken;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository);
        user = new User();
        user.setEmail("ali@mail.com");
    }

    private RefreshToken savedToken(String rawToken, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setTokenHash(EmailVerificationService.hash(rawToken));
        token.setExpiresAt(expiresAt);
        token.setUser(user);
        return token;
    }

    @Test
    void createSavesOnlyTheHash() {
        String rawToken = service.create(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertNotEquals(rawToken, saved.getTokenHash());
        assertEquals(EmailVerificationService.hash(rawToken), saved.getTokenHash());
        assertSame(user, saved.getUser());
    }

    @Test
    void rotateDeletesOldTokenAndReturnsNewOne() {
        RefreshToken old = savedToken("old-token", Instant.now().plus(RefreshTokenService.LIFETIME));
        when(refreshTokenRepository.findByTokenHash(EmailVerificationService.hash("old-token")))
                .thenReturn(Optional.of(old));

        RefreshTokenService.Rotated rotated = service.rotate("old-token");

        verify(refreshTokenRepository).delete(old);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertSame(user, rotated.user());
        assertNotEquals("old-token", rotated.token());
    }

    @Test
    void rotateRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.rotate("unknown"));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
    }

    @Test
    void rotateRejectsAndDeletesExpiredToken() {
        RefreshToken expired = savedToken("expired-token", Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(EmailVerificationService.hash("expired-token")))
                .thenReturn(Optional.of(expired));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.rotate("expired-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        verify(refreshTokenRepository).delete(expired);
    }
}
