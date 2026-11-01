package com.example.DevTutor.controller;

import com.example.DevTutor.dto.user.LearnerNoteResponse;
import com.example.DevTutor.dto.quiz.QuizResultResponse;
import com.example.DevTutor.dto.quiz.ReviewResponse;
import com.example.DevTutor.dto.user.UpdateUserRequest;
import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.mapper.UserMapper;
import com.example.DevTutor.repository.LearnerNoteRepository;
import com.example.DevTutor.repository.QuizResultRepository;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.service.ReviewScheduler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final QuizResultRepository quizResultRepository;
    private final LearnerNoteRepository learnerNoteRepository;
    private final ReviewScheduler reviewScheduler;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // The logged-in user's own profile (name, email, role).
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponse me(Authentication authentication) {
        return userMapper.toResponse(userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found")));
    }

    @GetMapping("/me/reviews")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<ReviewResponse> reviews(Authentication authentication) {
        return reviewScheduler.schedule(
                quizResultRepository.findByUserEmailOrderByCreatedAtDesc(authentication.getName()));
    }

    @GetMapping("/me/progress")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<QuizResultResponse> progress(Authentication authentication) {
        return quizResultRepository.findByUserEmailOrderByCreatedAtDesc(authentication.getName())
                .stream()
                .map(QuizResultResponse::from)
                .toList();
    }

    @GetMapping("/me/notes")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<LearnerNoteResponse> notes(Authentication authentication) {
        return learnerNoteRepository.findTop10ByUserEmailOrderByCreatedAtDesc(authentication.getName())
                .stream()
                .map(LearnerNoteResponse::from)
                .toList();
    }

    @DeleteMapping("/me/notes/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long id, Authentication authentication) {
        if (learnerNoteRepository.deleteByIdAndUserEmail(id, authentication.getName()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        User user = findUser(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
            user.setEmail(email);
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        User user = findUser(id);
        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException e) {
            // The user still owns conversations (created_by foreign key).
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User has conversations and cannot be deleted");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }
}
