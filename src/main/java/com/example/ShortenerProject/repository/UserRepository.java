package com.example.ShortenerProject.repository;

import com.example.ShortenerProject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}
