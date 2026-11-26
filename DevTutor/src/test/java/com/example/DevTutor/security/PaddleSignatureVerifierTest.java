package com.example.DevTutor.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaddleSignatureVerifierTest {

    private static final String SECRET = "test_secret";
    private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000L);
    private static final byte[] BODY = "{\"event_id\":\"evt_1\"}".getBytes(StandardCharsets.UTF_8);

    private final PaddleSignatureVerifier verifier = new PaddleSignatureVerifier(SECRET, 5);

    private String sign(long ts, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((ts + ":").getBytes(StandardCharsets.UTF_8));
        return "ts=" + ts + ";h1=" + HexFormat.of().formatHex(mac.doFinal(body));
    }

    @Test
    void validSignatureIsAccepted() throws Exception {
        assertTrue(verifier.isValid(BODY, sign(NOW.getEpochSecond(), BODY), NOW));
    }

    @Test
    void wrongSignatureIsRejected() {
        String header = "ts=" + NOW.getEpochSecond() + ";h1=" + "ab".repeat(32);
        assertFalse(verifier.isValid(BODY, header, NOW));
    }

    @Test
    void oldMessageIsRejected() throws Exception {
        String header = sign(NOW.getEpochSecond() - 6, BODY);
        assertFalse(verifier.isValid(BODY, header, NOW));
    }

    @Test
    void changedBodyIsRejected() throws Exception {
        String header = sign(NOW.getEpochSecond(), BODY);
        byte[] changed = "{\"event_id\":\"evt_2\"}".getBytes(StandardCharsets.UTF_8);
        assertFalse(verifier.isValid(changed, header, NOW));
    }
}
