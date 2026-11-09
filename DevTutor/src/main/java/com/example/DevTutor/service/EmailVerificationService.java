package com.example.DevTutor.service;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Service
public class EmailVerificationService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
    private static final Duration RATE_WINDOW = Duration.ofHours(1);
    private static final int MAX_PER_EMAIL = 3;
    private static final int MAX_PER_IP = 10;

    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String verifyUrl;
    private final String fromAddress;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Deque<Instant>> recentRequests = new HashMap<>();

    public EmailVerificationService(UserRepository userRepository,
                                    ObjectProvider<JavaMailSender> mailSender,
                                    @Value("${app.email-verification.url:http://localhost:8081/api/auth/verify-email}") String verifyUrl,
                                    @Value("${spring.mail.username:}") String fromAddress) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.verifyUrl = verifyUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerification(User user) {
        String rawToken = randomToken();
        user.setVerificationTokenHash(hash(rawToken));
        user.setVerificationTokenExpiresAt(Instant.now().plus(TOKEN_LIFETIME));
        userRepository.save(user);

        String link = UriComponentsBuilder.fromUriString(verifyUrl)
                .queryParam("token", rawToken)
                .toUriString();

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("Email sending is not configured. Verification link for {}: {}", user.getEmail(), link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(user.getEmail());
        message.setSubject("Verify your DevTutor email");
        message.setText("Hi " + user.getName() + ",\n\nClick this link to verify your email (valid for 24 hours):\n"
                + link + "\n\nIf you did not create a DevTutor account, ignore this email.");
        try {
            sender.send(message);
        } catch (MailException e) {
            log.warn("Could not send verification email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void resend(String email, String clientIp) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (!allowed("ip:" + clientIp, MAX_PER_IP) || !allowed("email:" + normalizedEmail, MAX_PER_EMAIL)) {
            return;
        }
        userRepository.findByEmail(normalizedEmail)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerification);
    }

    public boolean verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        User user = userRepository.findByVerificationTokenHash(hash(rawToken)).orElse(null);
        if (user == null) {
            return false;
        }
        boolean expired = user.getVerificationTokenExpiresAt() == null
                || user.getVerificationTokenExpiresAt().isBefore(Instant.now());

        user.setVerificationTokenHash(null);
        user.setVerificationTokenExpiresAt(null);
        if (!expired) {
            user.setEmailVerified(true);
        }
        userRepository.save(user);
        return !expired;
    }

    private synchronized boolean allowed(String key, int max) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(RATE_WINDOW);
        recentRequests.values().removeIf(times -> times.isEmpty() || times.peekLast().isBefore(windowStart));

        Deque<Instant> times = recentRequests.computeIfAbsent(key, k -> new ArrayDeque<>());
        while (!times.isEmpty() && times.peekFirst().isBefore(windowStart)) {
            times.pollFirst();
        }
        if (times.size() >= max) {
            return false;
        }
        times.addLast(now);
        return true;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
