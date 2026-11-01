package com.example.DevTutor.entity;

import com.example.DevTutor.enums.Roles;
import com.example.DevTutor.security.GitHubTokenConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role = Roles.USER;

    @Convert(converter = GitHubTokenConverter.class)
    private String githubToken;

    @Convert(converter = GitHubTokenConverter.class)
    @Column(columnDefinition = "TEXT")
    private String trelloAccessToken;

    @Convert(converter = GitHubTokenConverter.class)
    @Column(columnDefinition = "TEXT")
    private String trelloRefreshToken;

    private Instant trelloTokenExpiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Roles currentRole = role == null ? Roles.USER : role;
        return List.of(new SimpleGrantedAuthority("ROLE_" + currentRole.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}