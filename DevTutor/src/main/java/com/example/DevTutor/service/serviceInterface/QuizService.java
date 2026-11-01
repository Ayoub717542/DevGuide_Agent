package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.challenge.ChallengeResponse;
import com.example.DevTutor.dto.challenge.ChallengeResult;
import com.example.DevTutor.dto.quiz.QuizResponse;
import com.example.DevTutor.dto.quiz.QuizResultRequest;

public interface QuizService {
    QuizResponse generateQuiz(Long conversationId);
    void saveResult(Long conversationId, QuizResultRequest request);
    ChallengeResponse createChallenge(Long conversationId);
    ChallengeResult submitChallenge(Long challengeId, String code);
}
