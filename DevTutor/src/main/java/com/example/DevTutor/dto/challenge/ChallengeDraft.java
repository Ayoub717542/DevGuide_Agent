package com.example.DevTutor.dto.challenge;

public record ChallengeDraft(
        String title,
        String task,
        String starterCode,
        String testCode,
        String referenceSolution,
        String concept,
        String language) {
}
