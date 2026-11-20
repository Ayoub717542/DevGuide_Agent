package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.user.UpdateUserRequest;
import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.enums.Roles;
import com.example.DevTutor.mapper.UserMapper;
import com.example.DevTutor.repository.ChallengeRepository;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.LearnerNoteRepository;
import com.example.DevTutor.repository.LearnerPreferencesRepository;
import com.example.DevTutor.repository.QuizResultRepository;
import com.example.DevTutor.repository.RefreshTokenRepository;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.service.LearnerPreferencesService;
import com.example.DevTutor.service.ReviewScheduler;
import com.example.DevTutor.service.TrelloMcpService;
import com.example.DevTutor.service.TrelloOAuthService;
import com.example.DevTutor.service.serviceInterface.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String ADMIN_EMAIL = "admin@mail.com";

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private QuizResultRepository quizResultRepository;
    @Mock private LearnerNoteRepository learnerNoteRepository;
    @Mock private ReviewScheduler reviewScheduler;
    @Mock private LearnerPreferencesRepository learnerPreferencesRepository;
    @Mock private LearnerPreferencesService learnerPreferencesService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationService conversationService;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private TrelloOAuthService trelloOAuthService;
    @Mock private TrelloMcpService trelloMcpService;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserServiceImpl service;

    private User admin;
    private User sara;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setEmail(ADMIN_EMAIL);
        admin.setRole(Roles.ADMIN);

        sara = new User();
        sara.setId(2L);
        sara.setEmail("sara@mail.com");
        sara.setRole(Roles.USER);
    }

    @Test
    void adminDeleteRemovesAllUserData() {
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(sara));
        when(conversationRepository.findByCreatedByEmailOrderByCreatedAtDesc("sara@mail.com"))
                .thenReturn(List.of(conversation));

        service.deleteUser(2L, ADMIN_EMAIL);

        verify(trelloOAuthService).disconnect("sara@mail.com");
        verify(conversationService).deleteConversation(conversation);
        verify(challengeRepository).deleteByUserEmail("sara@mail.com");
        verify(learnerNoteRepository).deleteByUserEmail("sara@mail.com");
        verify(learnerPreferencesRepository).deleteByUserEmail("sara@mail.com");
        verify(quizResultRepository).deleteByUserEmail("sara@mail.com");
        verify(refreshTokenRepository).deleteByUser(sara);
        verify(userRepository).delete(sara);
    }

    @Test
    void adminCannotDeleteThemselves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteUser(1L, ADMIN_EMAIL));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void adminCannotChangeTheirOwnRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.updateUser(1L, new UpdateUserRequest(null, Roles.USER), ADMIN_EMAIL));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals(Roles.ADMIN, admin.getRole());
        verify(userRepository, never()).save(any());
    }

    @Test
    void roleChangeLogsTheUserOut() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sara));
        when(userRepository.save(sara)).thenReturn(sara);

        service.updateUser(2L, new UpdateUserRequest(null, Roles.ADMIN), ADMIN_EMAIL);

        assertEquals(Roles.ADMIN, sara.getRole());
        verify(refreshTokenRepository).deleteByUser(sara);
    }
}
