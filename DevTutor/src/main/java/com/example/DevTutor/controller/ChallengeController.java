package com.example.DevTutor.controller;

import com.example.DevTutor.dto.challenge.ChallengeResult;
import com.example.DevTutor.dto.challenge.ChallengeSubmitRequest;
import com.example.DevTutor.service.serviceInterface.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final QuizService quizService;

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ChallengeResult submit(
            @PathVariable Long id,
            @RequestBody ChallengeSubmitRequest request) {

        return quizService.submitChallenge(id, request.code());
    }
}
