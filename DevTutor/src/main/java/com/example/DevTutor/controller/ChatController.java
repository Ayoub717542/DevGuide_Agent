package com.example.DevTutor.controller;

import com.example.DevTutor.dto.chat.*;
import com.example.DevTutor.service.serviceInterface.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public MessageResponse reply(
            @RequestParam("conversationId") Long conversationId,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "hintMode", defaultValue = "false") boolean hintMode,
            @RequestParam(value = "devMode", defaultValue = "false") boolean devMode) {

        MessageRequest request = new MessageRequest(conversationId, content, hintMode, devMode);

        return chatService.reply(request, file);
    }

    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<MessageResponse> getMessages(@PathVariable Long id) {
        return chatService.getMessages(id);
    }
}