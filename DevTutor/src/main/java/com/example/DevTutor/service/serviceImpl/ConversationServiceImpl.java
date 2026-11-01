package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.chat.ConversationResponse;
import com.example.DevTutor.dto.chat.CreateConversationRequest;
import com.example.DevTutor.dto.chat.UpdateConversationRequest;
import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.Message;
import com.example.DevTutor.mapper.ConversationMapper;
import com.example.DevTutor.repository.ConversationCheckpointRepository;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.GeneratedDocumentRepository;
import com.example.DevTutor.repository.MessageRepository;
import com.example.DevTutor.repository.UploadedDocumentRepository;
import com.example.DevTutor.repository.UserRepository;
import com.example.DevTutor.service.serviceInterface.ConversationService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final ConversationCheckpointRepository checkpointRepository;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;
    private final Cloudinary cloudinary;

    @Override
    public ConversationResponse createConversation(
            CreateConversationRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title is required");
        }

        Conversation conversation = conversationMapper.toEntity(request);
        conversation.setCreatedBy(userRepository.findByEmail(currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found")));

        Conversation savedConversation =
                conversationRepository.save(conversation);

        return conversationMapper.toResponse(savedConversation);
    }

    @Override
    public List<ConversationResponse> getConversations() {
        return conversationMapper.toResponses(
                conversationRepository.findByCreatedByEmailOrderByCreatedAtDesc(currentUserEmail()));
    }

    @Override
    public ConversationResponse renameConversation(Long id, UpdateConversationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title is required");
        }

        Conversation conversation = findOwnConversation(id);
        conversation.setTitle(request.getTitle().trim());

        return conversationMapper.toResponse(conversationRepository.save(conversation));
    }

    @Override
    @Transactional
    public void deleteConversation(Long id) {
        Conversation conversation = findOwnConversation(id);
        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAscIdAsc(id);

        vectorStore.delete("conversationId == '" + id + "'");
        chatMemory.clear(id.toString());

        for (Message message : messages) {
            if (message.getImagePublicId() != null) {
                try {
                    cloudinary.uploader().destroy(message.getImagePublicId(), Map.of());
                } catch (Exception e) {
                    log.warn("Could not delete image {}", message.getImagePublicId(), e);
                }
            }
        }

        uploadedDocumentRepository.deleteByConversationId(id);
        generatedDocumentRepository.deleteByConversationId(id);
        checkpointRepository.deleteByConversationId(id);
        messageRepository.deleteAll(messages);
        conversationRepository.delete(conversation);
    }

    private Conversation findOwnConversation(Long id) {
        return conversationRepository
                .findByIdAndCreatedByEmail(id, currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));
    }

    private static String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
