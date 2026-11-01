package com.example.DevTutor.mapper;

import com.example.DevTutor.dto.user.UserResponse;
import com.example.DevTutor.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}