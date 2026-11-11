package com.example.DevTutor.dto.challenge;

import com.example.DevTutor.entity.Challenge;

public record ChallengeResponse(
        Long id,
        String title,
        String task,
        String starterCode,
        String language) {

    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getTask(),
                challenge.getStarterCode(),
                challenge.getLanguage());
    }
}
