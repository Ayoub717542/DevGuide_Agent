package com.example.DevTutor.dto.user;
import com.example.DevTutor.enums.Roles;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Roles role;
    private LocalDateTime createdAt;
}
