package com.example.DevTutor.dto.user;

import com.example.DevTutor.enums.Roles;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Fields left null are not changed.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Size(min = 2, max = 100)
    private String name;

    private Roles role;
}
