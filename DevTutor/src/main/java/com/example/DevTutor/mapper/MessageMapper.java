package com.example.DevTutor.mapper;
import com.example.DevTutor.dto.chat.MessageRequest;
import com.example.DevTutor.dto.chat.MessageResponse;
import com.example.DevTutor.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    Message toEntity(MessageRequest request);

    @Mapping(source = "conversation.id", target = "conversationId")
    MessageResponse toResponse(Message message);

    List<MessageResponse> toResponses(List<Message> messages);
}
