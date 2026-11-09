package com.example.DevTutor.mapper;

import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "githubConnected", expression = "java(user.getGithubToken() != null)")
    @Mapping(target = "trelloConnected", expression = "java(user.getTrelloAccessToken() != null)")
    UserResponse toResponse(User user);
}
