package com.example.DevTutor.security;

import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    private static final String EMAIL = "ali@mail.com";
    private static final String REDIRECT = "http://localhost:5173/oauth/callback";

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "redirectUrl", REDIRECT);
        OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
        when(client.getAccessToken()).thenReturn(new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "google-token", Instant.now(), Instant.now().plusSeconds(60)));
        when(authorizedClientService.loadAuthorizedClient("google", "123")).thenReturn(client);
    }

    private static OAuth2AuthenticationToken googleLogin(boolean emailVerified) {
        DefaultOAuth2User profile = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("sub", "123", "email", EMAIL, "email_verified", emailVerified, "name", "Ali"),
                "sub");
        return new OAuth2AuthenticationToken(profile, profile.getAuthorities(), "google");
    }

    @Test
    void blocksLinkingToUnverifiedPasswordAccount() throws Exception {
        User existing = new User();
        existing.setEmail(EMAIL);
        existing.setEmailVerified(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleLogin(true));

        assertEquals(REDIRECT + "#error=verify_email_first", response.getRedirectedUrl());
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
        verify(emailVerificationService).resend(EMAIL, "127.0.0.1");
    }

    @Test
    void rejectsUnverifiedGoogleEmail() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleLogin(false));

        assertEquals(REDIRECT + "#error=no_email", response.getRedirectedUrl());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void newOAuthUserIsMarkedVerified() throws Exception {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(jwtService.generateToken(any())).thenReturn("jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleLogin(true));

        assertEquals(REDIRECT + "#token=jwt", response.getRedirectedUrl());
        verify(userRepository).save(argThat(User::isEmailVerified));
    }
}
