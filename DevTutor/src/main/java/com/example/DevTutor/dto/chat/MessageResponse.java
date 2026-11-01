package com.example.DevTutor.dto.chat;

import com.example.DevTutor.dto.document.GeneratedDocumentResponse;

import com.example.DevTutor.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private MessageRole role;
    private LocalDateTime createdAt;
    private Long conversationId;
    private List<SourceResponse> sources;
    private String imageUrl;
    private List<GeneratedDocumentResponse> documents;
}
