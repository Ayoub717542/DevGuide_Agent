package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.challenge.ChallengeDraft;
import com.example.DevTutor.dto.challenge.ChallengeResponse;
import com.example.DevTutor.dto.challenge.ChallengeResult;
import lombok.extern.slf4j.Slf4j;
import com.example.DevTutor.dto.quiz.QuizResponse;
import com.example.DevTutor.dto.quiz.QuizResultRequest;
import com.example.DevTutor.entity.Challenge;
import com.example.DevTutor.entity.QuizResult;
import com.example.DevTutor.repository.ChallengeRepository;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.QuizResultRepository;
import com.example.DevTutor.service.ChallengeRunner;
import com.example.DevTutor.service.serviceInterface.QuizService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Slf4j
@Service
public class QuizServiceImpl implements QuizService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ConversationRepository conversationRepository;
    private final QuizResultRepository quizResultRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeRunner challengeRunner;

    public QuizServiceImpl(ChatClient.Builder builder,
                           ChatMemory chatMemory,
                           ConversationRepository conversationRepository,
                           QuizResultRepository quizResultRepository,
                           ChallengeRepository challengeRepository,
                           ChallengeRunner challengeRunner) {
        this.chatClient = builder.build();
        this.chatMemory = chatMemory;
        this.conversationRepository = conversationRepository;
        this.quizResultRepository = quizResultRepository;
        this.challengeRepository = challengeRepository;
        this.challengeRunner = challengeRunner;
    }

    @Override
    public QuizResponse generateQuiz(Long conversationId) {
        checkOwner(conversationId);
        String lesson = lesson(conversationId);

        return chatClient.prompt()
                .user("""
                        Write a multiple-choice quiz of exactly 3 questions
                        about what was taught in this lesson.
                        Each question has 4 options and one correct answer.
                        correctIndex starts at 0.
                        Keep the explanation to one short sentence.
                        Set topic to a short lowercase concept id, like java-streams or java-generics.
                        Always use the same id for the same concept.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(QuizResponse.class);
    }

    @Override
    public void saveResult(Long conversationId, QuizResultRequest request) {
        String email = checkOwner(conversationId);

        if (request.total() <= 0
                || request.score() < 0
                || request.score() > request.total()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Score must be between 0 and total.");
        }

        saveScore(request.topic(), request.score(), request.total(), email);
    }

    private void saveScore(String topic, int score, int total, String email) {
        QuizResult result = new QuizResult();
        result.setTopic(topic);
        result.setScore(score);
        result.setTotal(total);
        result.setUserEmail(email);
        quizResultRepository.save(result);
    }

    @Override
    public ChallengeResponse createChallenge(Long conversationId) {
        String email = checkOwner(conversationId);
        String lesson = lesson(conversationId);

        ChallengeDraft draft = null;
        for (int attempt = 0; attempt < 2 && draft == null; attempt++) {
            ChallengeDraft candidate = chatClient.prompt()
                .user("""
                        Create a small Java coding exercise about what was taught in this lesson.
                        The learner writes a class named Solution with one static method.
                        task: explain what to write, including the exact method signature.
                        starterCode: class Solution with that method and a body that only returns a default value.
                        referenceSolution: a complete and correct class Solution.
                        testCode: exactly 3 lines, each like:
                        check("test name", () -> Solution.method(args) == expected);
                        Use .equals for objects and Math.abs(a - b) < 0.001 for doubles.
                        One test must be an edge case. Only use java.util classes.
                        concept: a short lowercase concept id, like java-streams or java-generics.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(ChallengeDraft.class);

            ChallengeResult check = challengeRunner.run(candidate.testCode(), candidate.referenceSolution());
            if (check.status().equals("ok")) {
                draft = candidate;
            } else {
                log.warn("Challenge rejected ({}): {}", check.status(), check.output());
            }
        }

        if (draft == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not create a valid challenge. Please try again.");
        }

        Challenge challenge = new Challenge();
        challenge.setTitle(draft.title());
        challenge.setTask(draft.task());
        challenge.setStarterCode(draft.starterCode());
        challenge.setTestCode(draft.testCode());
        challenge.setReferenceSolution(draft.referenceSolution());
        challenge.setConcept(draft.concept());
        challenge.setUserEmail(email);

        return ChallengeResponse.from(challengeRepository.save(challenge));
    }

    @Override
    public ChallengeResult submitChallenge(Long challengeId, String code) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Challenge challenge = challengeRepository.findByIdAndUserEmail(challengeId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Challenge not found"));

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Write some code before submitting.");
        }

        ChallengeResult result = challengeRunner.run(challenge.getTestCode(), code);
        if (result.total() > 0) {
            saveScore(challenge.getConcept(), result.passed(), result.total(), email);
        }
        return result;
    }

    private String lesson(Long conversationId) {
        var messages = chatMemory.get(conversationId.toString());

        String lesson = messages.stream()
                .skip(Math.max(0, messages.size() - 6))
                .map(m -> m.getMessageType() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        if (lesson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chat first, then ask for a quiz or a challenge.");
        }
        return lesson;
    }

    private String checkOwner(Long conversationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        conversationRepository.findByIdAndCreatedByEmail(conversationId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));
        return email;
    }
}
