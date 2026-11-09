package com.example.DevTutor.service;

import com.example.DevTutor.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TrelloOAuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TrelloOAuthService service =
            new TrelloOAuthService(userRepository, "client-id", "http://localhost:8081/api/trello/callback");

    private String startConnect(String email) {
        return UriComponentsBuilder.fromUriString(service.authorizeUrl(email))
                .build().getQueryParams().getFirst("state");
    }

    @Test
    void anotherUserCannotFinishSomeoneElsesLink() {
        String bobState = startConnect("bob@mail.com");

        assertFalse(service.finishConnect("code", bobState, "alice@mail.com"));
        verifyNoInteractions(userRepository);
    }

    @Test
    void linkWorksOnlyOnce() {
        String state = startConnect("bob@mail.com");
        service.finishConnect("code", state, "alice@mail.com");

        assertFalse(service.finishConnect("code", state, "bob@mail.com"));
        verifyNoInteractions(userRepository);
    }
}
