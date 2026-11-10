package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.chat.ConversationResponse;
import com.example.DevTutor.dto.chat.CreateConversationRequest;
import com.example.DevTutor.dto.chat.MessageRequest;
import com.example.DevTutor.dto.chat.MessageResponse;
import com.example.DevTutor.entity.Conversation;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public interface ChatService {
    MessageResponse reply(MessageRequest request, MultipartFile file);
    Flux<String> replyStream(MessageRequest request, MultipartFile file);
    List<MessageResponse> getMessages(Long conversationId);
}