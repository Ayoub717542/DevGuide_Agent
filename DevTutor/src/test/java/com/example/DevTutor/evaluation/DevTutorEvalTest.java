package com.example.DevTutor.evaluation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIfEnvironmentVariable(named = "EVAL_EMAIL", matches = ".+")
class DevTutorEvalTest {

    private final RestClient api = RestClient.create(
            System.getenv().getOrDefault("EVAL_URL", "http://localhost:8081"));

    @Test
    void q2InterfaceVsAbstractClass() {
        String answer = ask("eval Q2",
                "What is the difference between an interface and an abstract class in Java?");

        System.out.println("Q2 answer:\n" + answer);
        assertFalse(answer.isBlank());
    }

    private String ask(String title, String question) {
        String token = api.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", System.getenv("EVAL_EMAIL"),
                        "password", System.getenv("EVAL_PASSWORD")))
                .retrieve().body(Map.class).get("token").toString();

        Object conversationId = api.post().uri("/api/chat/conversations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", title))
                .retrieve().body(Map.class).get("id");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("conversationId", conversationId.toString());
        form.add("content", question);

        return api.post().uri("/api/chat")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve().body(Map.class).get("content").toString();
    }
}
