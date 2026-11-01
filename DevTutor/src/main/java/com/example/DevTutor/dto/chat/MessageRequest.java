package com.example.DevTutor.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private Long conversationId;
    private String content;
    private boolean hintMode;
    private boolean devMode;
}
