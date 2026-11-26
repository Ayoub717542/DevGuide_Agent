package com.example.DevTutor.entity;

import com.example.DevTutor.enums.Roles;
import com.example.DevTutor.enums.SubscriptionStatus;
import com.example.DevTutor.security.GitHubTokenConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
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

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean emailVerified;

    @Column(unique = true, length = 64)
    private String verificationTokenHash;

    private Instant verificationTokenExpiresAt;

    @Column(unique = true, length = 64)
    private String resetTokenHash;

    private Instant resetTokenExpiresAt;

    @Convert(converter = GitHubTokenConverter.class)
    private String githubToken;

    @Convert(converter = GitHubTokenConverter.class)
    @Column(columnDefinition = "TEXT")
    private String trelloAccessToken;

    @Convert(converter = GitHubTokenConverter.class)
    @Column(columnDefinition = "TEXT")
    private String trelloRefreshToken;

    private Instant trelloTokenExpiresAt;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Column(unique = true)
    private String paddleSubscriptionId;

    private Instant subscriptionEventAt;

    private Instant cancelEffectiveAt;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int messagesToday;

    private LocalDate messagesDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }




    public boolean hasProAccess() {
        if (role == Roles.ADMIN) {
            return true;
        }
        if (cancelEffectiveAt != null && !Instant.now().isBefore(cancelEffectiveAt)) {
            return false;
        }
        return subscriptionStatus == SubscriptionStatus.ACTIVE
                || subscriptionStatus == SubscriptionStatus.TRIALING
                || subscriptionStatus == SubscriptionStatus.PAST_DUE;
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



    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}