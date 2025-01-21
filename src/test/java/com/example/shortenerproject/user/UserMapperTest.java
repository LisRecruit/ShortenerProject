package com.example.shortenerproject.user;

import com.example.shortenerproject.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapUserToUserResponse() {
        User user = new User();
        user.setUsername("testuser");
        user.setId(1L);

        UserResponse userResponse = userMapper.toUserResponse(user);

        assertEquals("testuser", userResponse.username());
        assertEquals(1L, userResponse.id());
    }

}