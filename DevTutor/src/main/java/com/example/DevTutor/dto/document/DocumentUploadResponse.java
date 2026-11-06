package com.example.DevTutor.dto.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private String documentId;
    private String filename;
    private int chunksStored;
    private Long conversationId;
    private String conversationTitle;
    private LocalDateTime createdAt;
}