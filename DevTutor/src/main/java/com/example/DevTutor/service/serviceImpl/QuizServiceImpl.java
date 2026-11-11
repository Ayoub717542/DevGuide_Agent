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
import com.example.DevTutor.util.Exceptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;

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

        for (int attempt = 0; attempt < 2; attempt++) {
            QuizResponse quiz = askQuiz(lesson);
            if (isValid(quiz)) {
                return quiz;
            }
            log.warn("Quiz rejected (attempt {}): {}", attempt + 1, quiz);
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not create a valid quiz. Please try again.");
    }

    private QuizResponse askQuiz(String lesson) {
        try {
            return chatClient.prompt()
                .user("""
                        Write a multiple-choice quiz of exactly 3 questions
                        about what was taught in this lesson.
                        Each question has 4 options and one correct answer.
                        correctIndex starts at 0.
                        Keep the explanation to one short sentence.
                        Set topic to a short lowercase concept id that starts with the language, like python-lists, js-promises or java-streams.
                        Always use the same id for the same concept.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(QuizResponse.class);
        } catch (IllegalStateException e) {
            if (Exceptions.findCause(e, JacksonException.class).isEmpty()) {
                throw e;
            }
            log.warn("Quiz reply was not valid JSON: {}", e.getMessage());
            return null;
        }
    }

    static boolean isValid(QuizResponse quiz) {
        return quiz != null && quiz.questions() != null && quiz.questions().size() == 3
                && quiz.questions().stream().allMatch(q -> q != null
                        && q.question() != null && !q.question().isBlank()
                        && q.options() != null && q.options().size() == 4
                        && q.options().stream().allMatch(o -> o != null && !o.isBlank())
                        && q.correctIndex() >= 0 && q.correctIndex() < 4);
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
                        Create a small coding exercise about what was taught in this lesson.
                        If the lesson contains the learner's own code, build the exercise on that code and reuse its names (classes, fields, methods).
                        language: the language of the lesson, exactly one of java, python or javascript. Use java if the lesson uses another language.
                        task: explain what to write, including the exact method or function signature.
                        starterCode: the method or function with a body that only returns a default value.
                        referenceSolution: a complete and correct solution.
                        testCode: exactly 3 lines, one test per line. One test must be an edge case.

                        If java: the learner writes a class named Solution with one static method.
                        Each test line is like: check("test name", () -> Solution.method(args) == expected);
                        Use .equals for objects and Math.abs(a - b) < 0.001 for doubles. Only use java.util classes.

                        If python: the learner writes one function, no class.
                        Each test line is like: check("test name", lambda: function(args) == expected)
                        Use abs(a - b) < 0.001 for floats. Only use the standard library.

                        If javascript: the learner writes one function, no class.
                        Each test line is like: check("test name", () => fn(args) === expected);
                        Compare arrays and objects with JSON.stringify. Do not use require or import.

                        concept: a short lowercase concept id that starts with the language, like java-streams, python-lists or js-promises.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(ChallengeDraft.class);

            ChallengeResult check = challengeRunner.run(language(candidate), candidate.testCode(), candidate.referenceSolution());
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
        challenge.setLanguage(language(draft));
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

        ChallengeResult result = challengeRunner.run(challenge.getLanguage(), challenge.getTestCode(), code);
        if (result.total() > 0) {
            saveScore(challenge.getConcept(), result.passed(), result.total(), email);
        }
        return result;
    }

    private String language(ChallengeDraft draft) {
        return draft.language() == null ? "java" : draft.language().strip().toLowerCase();
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
