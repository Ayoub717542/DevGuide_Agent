package com.example.DevTutor.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordRequestDTO {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;
}
