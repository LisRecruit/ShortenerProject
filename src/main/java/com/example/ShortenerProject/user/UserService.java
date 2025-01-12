package com.example.ShortenerProject.user;

import com.example.ShortenerProject.user.dto.request.UserCreateRequest;
import com.example.ShortenerProject.user.dto.response.UserResponse;
import com.example.ShortenerProject.user.dto.request.UserUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String NOT_FOUND=" not found";
    private final UserRepository userRepository;
    private final UserMapper userMapper;
//    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return "User with this username already exists";
        }

        User user = User.builder()
                .username(request.username())
                .password(request.password()) //додати passwordEncoder з SecurityConfig
                .build();
        userRepository.save(user);
        return "User created";
    }

    public Page<UserResponse> getAllUsers(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest)
                .map(userMapper::toUserResponse);
    }

    public UserResponse getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("User with id " + id + NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " NOT FOUND"));
    }


    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findForUpdateById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + NOT_FOUND));
        user.setUsername(request.username());
        return userMapper.toUserResponse(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
