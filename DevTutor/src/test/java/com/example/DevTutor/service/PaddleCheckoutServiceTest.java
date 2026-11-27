package com.example.DevTutor.service;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.enums.SubscriptionStatus;
import com.example.DevTutor.repository.PaddleCheckoutRepository;
import com.example.DevTutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaddleCheckoutServiceTest {

    private static final String EMAIL = "user@test.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaddleCheckoutRepository checkoutRepository;

    @Mock
    private PaddleClient paddleClient;

    private PaddleCheckoutService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PaddleCheckoutService(userRepository, checkoutRepository, paddleClient);
        user = new User();
        user.setEmail(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void activeSubscriptionIsCanceled() {
        user.setPaddleSubscriptionId("sub_01abc");
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

        service.cancel(EMAIL);

        verify(paddleClient).cancelSubscription("sub_01abc");
    }

    @Test
    void userWithoutSubscriptionGetsConflict() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.cancel(EMAIL));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(paddleClient, never()).cancelSubscription(any());
    }

    @Test
    void alreadyScheduledCancelGetsConflict() {
        user.setPaddleSubscriptionId("sub_01abc");
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        user.setCancelEffectiveAt(Instant.parse("2026-12-27T00:00:00Z"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.cancel(EMAIL));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(paddleClient, never()).cancelSubscription(any());
    }
}
