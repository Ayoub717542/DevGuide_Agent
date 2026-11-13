package com.example.DevTutor.auth;

import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.dto.auth.AuthenticationRequestDTO;
import com.example.DevTutor.dto.auth.AuthenticationResponceDTO;
import com.example.DevTutor.dto.auth.RegisterRequoestDTO;
import com.example.DevTutor.dto.auth.ResendVerificationRequestDTO;
import com.example.DevTutor.dto.auth.ResetPasswordRequestDTO;
import com.example.DevTutor.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationContoller {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequoestDTO request) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public AuthenticationResponceDTO login(@Valid @RequestBody AuthenticationRequestDTO request) {
        return authenticationService.login(request);
    }

    @GetMapping("/verify-email")
    public Map<String, String> verifyEmail(@RequestParam(required = false) String token) {
        if (!emailVerificationService.verify(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This link is invalid or expired. Ask for a new one.");
        }
        return Map.of("message", "Email verified. You can now link GitHub or Google.");
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> resendVerification(@Valid @RequestBody ResendVerificationRequestDTO request,
                                                  HttpServletRequest httpRequest) {
        emailVerificationService.resend(request.getEmail(), httpRequest.getRemoteAddr());
        return Map.of("message", "If this email needs verification, a new link has been sent.");
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> forgotPassword(@Valid @RequestBody ResendVerificationRequestDTO request,
                                              HttpServletRequest httpRequest) {
        emailVerificationService.sendPasswordReset(request.getEmail(), httpRequest.getRemoteAddr());
        return Map.of("message", "If this email has an account, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        if (!emailVerificationService.resetPassword(request.getToken(), request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This link is invalid or expired. Ask for a new one.");
        }
        return Map.of("message", "Password changed. You can now log in.");
    }
}
