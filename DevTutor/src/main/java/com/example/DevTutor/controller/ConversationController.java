package com.example.DevTutor.controller;

import com.example.DevTutor.dto.challenge.ChallengeResponse;
import com.example.DevTutor.dto.chat.ConversationResponse;
import com.example.DevTutor.dto.chat.CreateConversationRequest;
import com.example.DevTutor.dto.quiz.QuizResponse;
import com.example.DevTutor.dto.quiz.QuizResultRequest;
import com.example.DevTutor.dto.chat.UpdateConversationRequest;
import com.example.DevTutor.service.serviceInterface.ConversationService;
import com.example.DevTutor.service.serviceInterface.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final QuizService quizService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ConversationResponse createConversation(
            @RequestBody CreateConversationRequest request) {

        return conversationService.createConversation(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<ConversationResponse> getConversations() {
        return conversationService.getConversations();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ConversationResponse renameConversation(
            @PathVariable Long id,
            @RequestBody UpdateConversationRequest request) {

        return conversationService.renameConversation(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
    }

    @PostMapping("/{id}/quiz")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public QuizResponse generateQuiz(@PathVariable Long id) {
        return quizService.generateQuiz(id);
    }

    @PostMapping("/{id}/challenge")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ChallengeResponse createChallenge(@PathVariable Long id) {
        return quizService.createChallenge(id);
    }

    @PostMapping("/{id}/quiz/result")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveQuizResult(
            @PathVariable Long id,
            @RequestBody QuizResultRequest request) {

        quizService.saveResult(id, request);
    }
}
