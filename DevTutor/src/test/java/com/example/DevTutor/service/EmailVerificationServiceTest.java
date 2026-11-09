package com.example.DevTutor.service;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String EMAIL = "ali@mail.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(userRepository, mailSenderProvider,
                "http://localhost:8081/api/auth/verify-email", "devtutor@mail.com");
        user = new User();
        user.setEmail(EMAIL);
        user.setName("Ali");
    }

    private String sendAndCaptureToken() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        service.sendVerification(user);
        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        String text = mail.getValue().getText();
        return text.substring(text.indexOf("token=") + 6).split("\\s")[0];
    }

    @Test
    void storesOnlyTheHashOfTheToken() {
        String rawToken = sendAndCaptureToken();

        assertNotEquals(rawToken, user.getVerificationTokenHash());
        assertEquals(EmailVerificationService.hash(rawToken), user.getVerificationTokenHash());
        assertTrue(user.getVerificationTokenExpiresAt().isAfter(Instant.now().plusSeconds(23 * 3600)));
    }

    @Test
    void validTokenVerifiesOnceOnly() {
        String rawToken = sendAndCaptureToken();
        String hash = user.getVerificationTokenHash();
        when(userRepository.findByVerificationTokenHash(hash)).thenReturn(Optional.of(user));

        assertTrue(service.verify(rawToken));
        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationTokenHash());

        when(userRepository.findByVerificationTokenHash(hash)).thenReturn(Optional.empty());
        assertFalse(service.verify(rawToken));
    }

    @Test
    void expiredTokenIsRejected() {
        String rawToken = sendAndCaptureToken();
        user.setVerificationTokenExpiresAt(Instant.now().minusSeconds(1));
        when(userRepository.findByVerificationTokenHash(user.getVerificationTokenHash())).thenReturn(Optional.of(user));

        assertFalse(service.verify(rawToken));
        assertFalse(user.isEmailVerified());
    }

    @Test
    void resendIsRateLimitedPerEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        for (int i = 0; i < 5; i++) {
            service.resend(EMAIL, "1.2.3.4");
        }

        verify(userRepository, times(3)).save(any(User.class));
    }
}
