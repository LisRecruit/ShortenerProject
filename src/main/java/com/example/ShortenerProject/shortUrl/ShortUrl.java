package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "short_urls")
@Data
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "short_url", unique = true, nullable = false)
    @JsonProperty("shortUrl")
    private String shortUrl;

    @JsonProperty("originUrl")
    @Column(name = "origin_url", nullable = false)
    private String originUrl;

    @Column(name = "date_of_creating")
    private String dateOfCreating;

    @Column(name = "date_of_expiring")
    private String dateOfExpiring;

    @Column(name = "count_of_transition")
    private long countOfTransition;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
}
