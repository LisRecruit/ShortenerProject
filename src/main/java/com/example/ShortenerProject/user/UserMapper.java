package com.example.ShortenerProject.user;

import com.example.ShortenerProject.user.dto.request.UserCreateRequest;
import com.example.ShortenerProject.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "username", source = "user.username")
    UserResponse toUserResponse(User user);
}
