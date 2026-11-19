package com.example.DevTutor.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponceDTO {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
