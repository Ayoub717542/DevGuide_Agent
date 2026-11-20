package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.chat.ConversationResponse;
import com.example.DevTutor.dto.chat.CreateConversationRequest;
import com.example.DevTutor.dto.chat.UpdateConversationRequest;
import com.example.DevTutor.entity.Conversation;

import java.util.List;

public interface ConversationService {
    ConversationResponse createConversation(CreateConversationRequest request);
    List<ConversationResponse> getConversations();
    ConversationResponse renameConversation(Long id, UpdateConversationRequest request);
    void deleteConversation(Long id);
    void deleteConversation(Conversation conversation);
}
