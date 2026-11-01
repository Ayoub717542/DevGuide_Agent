package com.example.DevTutor.mapper;

import com.example.DevTutor.dto.chat.ConversationResponse;
import com.example.DevTutor.dto.chat.CreateConversationRequest;
import com.example.DevTutor.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Conversation toEntity(CreateConversationRequest request);

    @Mapping(source = "createdBy.id", target = "createdById")
    ConversationResponse toResponse(Conversation conversation);

    List<ConversationResponse> toResponses(List<Conversation> conversations);
}