package com.example.ShortenerProject.user;

import com.example.ShortenerProject.shortUrl.ShortUrl;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", unique = true, nullable = false)
    private String password;

    @OneToMany(mappedBy = "user")
    List<ShortUrl> urls = new ArrayList<ShortUrl>();
}
