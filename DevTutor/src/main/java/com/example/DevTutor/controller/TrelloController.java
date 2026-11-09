package com.example.DevTutor.controller;

import com.example.DevTutor.service.TrelloMcpService;
import com.example.DevTutor.service.TrelloOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trello")
@RequiredArgsConstructor
public class TrelloController {

    private final TrelloOAuthService trelloOAuthService;
    private final TrelloMcpService trelloMcpService;

    @Value("${app.trello.done-url}")
    private String doneUrl;

    @GetMapping("/connect")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, String> connect(Authentication authentication) {
        return Map.of(
                "url", trelloOAuthService.authorizeUrl(authentication.getName()),
                "help", "You need a Trello workspace first. New to Trello? Open https://trello.com, "
                        + "create a workspace, then connect again.");
    }

    @GetMapping("/callback")
    public void callback(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         HttpServletResponse response) throws IOException {
        if (code == null || state == null) {
            response.sendRedirect(doneUrl + "#trello=error");
            return;
        }
        response.sendRedirect(doneUrl + "#trello_code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&trello_state=" + URLEncoder.encode(state, StandardCharsets.UTF_8));
    }

    @PostMapping("/finish")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, Boolean> finish(@RequestBody Map<String, String> body, Authentication authentication) {
        return Map.of("connected",
                trelloOAuthService.finishConnect(body.get("code"), body.get("state"), authentication.getName()));
    }

    @GetMapping("/tools")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<String> tools(Authentication authentication) {
        return trelloMcpService.toolsFor(authentication.getName()).stream()
                .map(tool -> tool.getToolDefinition().name())
                .toList();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<TrelloMcpService.PendingAction> pending(Authentication authentication) {
        return trelloMcpService.pendingFor(authentication.getName());
    }

    @PostMapping("/pending/{id}/approve")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, String> approve(@PathVariable String id, Authentication authentication) {
        return Map.of("result", trelloMcpService.approve(id, authentication.getName()));
    }

    @DeleteMapping("/pending/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable String id, Authentication authentication) {
        trelloMcpService.reject(id, authentication.getName());
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(Authentication authentication) {
        trelloMcpService.forget(authentication.getName());
        trelloOAuthService.disconnect(authentication.getName());
    }
}
