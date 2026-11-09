package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.quiz.QuizResultResponse;
import com.example.DevTutor.dto.quiz.ReviewResponse;
import com.example.DevTutor.dto.user.ChangePasswordRequest;
import com.example.DevTutor.dto.user.LearnerNoteResponse;
import com.example.DevTutor.dto.user.PreferencesRequest;
import com.example.DevTutor.dto.user.PreferencesResponse;
import com.example.DevTutor.dto.user.UpdateNameRequest;
import com.example.DevTutor.dto.user.UpdateUserRequest;
import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.mapper.UserMapper;
import com.example.DevTutor.repository.LearnerNoteRepository;
import com.example.DevTutor.repository.LearnerPreferencesRepository;
import com.example.DevTutor.repository.QuizResultRepository;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.service.LearnerPreferencesService;
import com.example.DevTutor.service.ReviewScheduler;
import com.example.DevTutor.service.serviceInterface.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserServiceInterface {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final QuizResultRepository quizResultRepository;
    private final LearnerNoteRepository learnerNoteRepository;
    private final ReviewScheduler reviewScheduler;
    private final LearnerPreferencesRepository learnerPreferencesRepository;
    private final LearnerPreferencesService learnerPreferencesService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getMe(String email) {
        return userMapper.toResponse(findUser(email));
    }

    @Override
    public UserResponse updateMyName(String email, UpdateNameRequest updateNameRequest) {
        User user = findUser(email);
        user.setName(updateNameRequest.name().trim());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUser(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is wrong");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public List<ReviewResponse> getReviews(String email) {
        return reviewScheduler.schedule(quizResultRepository.findByUserEmailOrderByCreatedAtDesc(email));
    }

    @Override
    public List<QuizResultResponse> getProgress(String email) {
        return quizResultRepository.findByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(QuizResultResponse::from)
                .toList();
    }

    @Override
    public List<LearnerNoteResponse> getNotes(String email) {
        return learnerNoteRepository.findTop10ByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(LearnerNoteResponse::from)
                .toList();
    }

    @Override
    public void deleteNote(Long id, String email) {
        if (learnerNoteRepository.deleteByIdAndUserEmail(id, email) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
        }
    }

    @Override
    public PreferencesResponse getPreferences(String email) {
        return learnerPreferencesRepository.findByUserEmail(email)
                .map(PreferencesResponse::from)
                .orElse(new PreferencesResponse(null, null, null, null, null));
    }

    @Override
    public PreferencesResponse updatePreferences(String email, PreferencesRequest request) {
        return PreferencesResponse.from(learnerPreferencesService.update(email, request));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
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

    @Override
    public void deleteUser(Long id) {
        User user = findUser(id);
        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User has conversations and cannot be deleted");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
