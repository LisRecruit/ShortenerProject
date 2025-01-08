package com.example.ShortenerProject.repository;

import com.example.ShortenerProject.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
}
