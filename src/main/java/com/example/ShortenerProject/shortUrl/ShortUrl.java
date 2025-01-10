package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "short_urls")
@Data
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "short_url", unique = true, nullable = false)
    private String shortUrl;

    @Column(name = "origin_url", nullable = false)
    private String originUrl;

    @Column(name = "date_of_creating")
    private LocalDate dateOfCreating;

    @Column(name = "date_of_expiring")
    private LocalDate dateOfExpiring;

    @Column(name = "count_of_transition")
    private long countOfTransition;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
}
