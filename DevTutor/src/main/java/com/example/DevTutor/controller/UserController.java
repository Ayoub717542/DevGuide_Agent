package com.example.DevTutor.controller;

import com.example.DevTutor.dto.quiz.QuizResultResponse;
import com.example.DevTutor.dto.quiz.ReviewResponse;
import com.example.DevTutor.dto.user.ChangePasswordRequest;
import com.example.DevTutor.dto.user.LearnerNoteResponse;
import com.example.DevTutor.dto.user.PreferencesRequest;
import com.example.DevTutor.dto.user.PreferencesResponse;
import com.example.DevTutor.dto.user.UpdateNameRequest;
import com.example.DevTutor.dto.user.UpdateUserRequest;
import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.service.serviceInterface.UserServiceInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceInterface userServiceInterface;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userServiceInterface.getAllUsers();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponse me(Authentication authentication) {
        return userServiceInterface.getMe(authentication.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateNameRequest request) {
        return userServiceInterface.updateMyName(authentication.getName(), request);
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        userServiceInterface.changePassword(authentication.getName(), request);
    }

    @DeleteMapping("/me/github")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnectGithub(Authentication authentication) {
        userServiceInterface.disconnectGithub(authentication.getName());
    }

    @GetMapping("/me/reviews")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<ReviewResponse> reviews(Authentication authentication) {
        return userServiceInterface.getReviews(authentication.getName());
    }

    @GetMapping("/me/progress")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<QuizResultResponse> progress(Authentication authentication) {
        return userServiceInterface.getProgress(authentication.getName());
    }

    @GetMapping("/me/notes")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<LearnerNoteResponse> notes(Authentication authentication) {
        return userServiceInterface.getNotes(authentication.getName());
    }

    @GetMapping("/me/preferences")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PreferencesResponse preferences(Authentication authentication) {
        return userServiceInterface.getPreferences(authentication.getName());
    }

    @PutMapping("/me/preferences")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PreferencesResponse updatePreferences(Authentication authentication,
                                                 @RequestBody PreferencesRequest request) {
        return userServiceInterface.updatePreferences(authentication.getName(), request);
    }

    @DeleteMapping("/me/notes/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long id, Authentication authentication) {
        userServiceInterface.deleteNote(id, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        return userServiceInterface.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userServiceInterface.deleteUser(id);
    }
}
