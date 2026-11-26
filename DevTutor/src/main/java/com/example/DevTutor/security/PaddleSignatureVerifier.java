package com.example.DevTutor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Component
public class PaddleSignatureVerifier {

    private final String secret;
    private final long toleranceSeconds;

    public PaddleSignatureVerifier(@Value("${app.paddle.webhook-secret}") String secret,
                                   @Value("${app.paddle.signature-tolerance-seconds}") long toleranceSeconds) {
        this.secret = secret;
        this.toleranceSeconds = toleranceSeconds;
    }

    public boolean isValid(byte[] rawBody, String signatureHeader) {
        return isValid(rawBody, signatureHeader, Instant.now());
    }

    boolean isValid(byte[] rawBody, String signatureHeader, Instant now) {
        if (secret == null || secret.isBlank() || rawBody == null || signatureHeader == null) {
            return false;
        }

        String ts = null;
        List<String> signatures = new ArrayList<>();
        for (String part : signatureHeader.split(";")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            if (keyValue[0].equals("ts")) {
                ts = keyValue[1];
            } else if (keyValue[0].equals("h1")) {
                signatures.add(keyValue[1]);
            }
        }
        if (ts == null || signatures.isEmpty()) {
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(ts);
        } catch (NumberFormatException e) {
            return false;
        }
        if (Math.abs(now.getEpochSecond() - timestamp) > toleranceSeconds) {
            return false;
        }

        byte[] expected = hmacSha256(ts, rawBody);
        for (String signature : signatures) {
            byte[] received;
            try {
                received = HexFormat.of().parseHex(signature);
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (MessageDigest.isEqual(expected, received)) {
                return true;
            }
        }
        return false;
    }

    private byte[] hmacSha256(String ts, byte[] rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((ts + ":").getBytes(StandardCharsets.UTF_8));
            return mac.doFinal(rawBody);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }
}
