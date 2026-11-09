package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.quiz.QuizResultResponse;
import com.example.DevTutor.dto.quiz.ReviewResponse;
import com.example.DevTutor.dto.user.ChangePasswordRequest;
import com.example.DevTutor.dto.user.LearnerNoteResponse;
import com.example.DevTutor.dto.user.PreferencesRequest;
import com.example.DevTutor.dto.user.PreferencesResponse;
import com.example.DevTutor.dto.user.UpdateNameRequest;
import com.example.DevTutor.dto.user.UpdateUserRequest;
import com.example.DevTutor.dto.user.UserResponse;

import java.util.List;

public interface UserServiceInterface {

    List<UserResponse> getAllUsers();

    UserResponse getMe(String email);

    UserResponse updateMyName(String email, UpdateNameRequest updateNameRequest);

    void changePassword(String email, ChangePasswordRequest request);

    void disconnectGithub(String email);

    List<ReviewResponse> getReviews(String email);

    List<QuizResultResponse> getProgress(String email);

    List<LearnerNoteResponse> getNotes(String email);

    void deleteNote(Long id, String email);

    PreferencesResponse getPreferences(String email);

    PreferencesResponse updatePreferences(String email, PreferencesRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
