package com.example.DevTutor.auth;

import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.dto.auth.AuthenticationRequestDTO;
import com.example.DevTutor.dto.auth.AuthenticationResponceDTO;
import com.example.DevTutor.dto.auth.RegisterRequoestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationContoller {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequoestDTO request) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public AuthenticationResponceDTO login(@Valid @RequestBody AuthenticationRequestDTO request) {
        return authenticationService.login(request);
    }
}
