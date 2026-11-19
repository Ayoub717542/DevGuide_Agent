package com.example.DevTutor.auth;

import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.dto.auth.AuthenticationRequestDTO;
import com.example.DevTutor.dto.auth.AuthenticationResponceDTO;
import com.example.DevTutor.dto.auth.RegisterRequoestDTO;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.mapper.UserMapper;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.security.JwtService;
import com.example.DevTutor.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public UserResponse register(RegisterRequoestDTO request) {
        String email = request.getEmail();
        if (email != null) {
            email = email.trim().toLowerCase();
        }

        String name = request.getName();
        if (name != null) {
            name = name.trim();
        }

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        emailVerificationService.sendVerification(saved);
        return userMapper.toResponse(saved);
    }

    public AuthenticationResponceDTO login(AuthenticationRequestDTO request, String clientIp) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            boolean passwordCorrect = request.getEmail() != null && request.getPassword() != null
                    && userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                    .map(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                    .orElse(false);
            if (passwordCorrect) {
                emailVerificationService.resend(request.getEmail(), clientIp);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Please verify your email first. We just sent a new link to your inbox.");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return new AuthenticationResponceDTO(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
