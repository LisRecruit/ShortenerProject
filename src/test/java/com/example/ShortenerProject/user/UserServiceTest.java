package com.example.ShortenerProject.user;

import com.example.ShortenerProject.user.dto.request.UserCreateRequest;
import com.example.ShortenerProject.user.dto.request.UserUpdateRequest;
import com.example.ShortenerProject.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    public UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        UserCreateRequest user = new UserCreateRequest("testUser", "password");
        when(userRepository.existsByUsername(user.username())).thenReturn(false);
        when(passwordEncoder.encode(user.password())).thenReturn("encodedPassword");

        String createdUser = userService.createUser(user);

        assertEquals("User with username testUser created", createdUser);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        User user = new User(1L, "testUser", "encodedPassword");
        UserResponse userResponse = new UserResponse(1L, "testUser");
        Page<User> usersPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageRequest)).thenReturn(usersPage);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.getAllUsers(pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals("testUser", result.getContent().getFirst().username());
        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void testGetUserById() {
        User user = new User(1L, "testUser", "encodedPassword");
        UserResponse userResponse = new UserResponse(1L, "testUser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testUser", result.username());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserByUsername() {
        User user = new User(1L, "testUser", "encodedPassword");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void testUpdateUser() {
        User user = new User(1L, "testUser", "encodedPassword");
        UserUpdateRequest request = new UserUpdateRequest("updatedUser");
        UserResponse userResponse = new UserResponse(1L, "updatedUser");

        when(userRepository.findForUpdateById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, request);

        assertNotNull(result);
        assertEquals("updatedUser", result.username());
        verify(userRepository, times(1)).findForUpdateById(1L);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
