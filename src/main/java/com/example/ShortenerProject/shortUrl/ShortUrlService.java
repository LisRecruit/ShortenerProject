package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    /**
     * Creates a new short URL and saves it in the database.
     *
     * @param shortUrl The ShortUrl object
     * @return The saved ShortUrl object
     */
    public ShortUrl createShortUrl(ShortUrl shortUrl) {
        return shortUrlRepository.save(shortUrl);
    }

    /**
     * Returns all short URLs of the user.
     *
     * @param user The user object
     * @return The list of the user's URLs
     */
    public List<ShortUrl> getAllShortUrlsByUser(User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> url.getUser().getId() == user.getId())
                .toList();
    }

    /**
     * Finds the short URL by its identifier.
     *
     * @param id The identifier of the URL
     * @param user The user object
     * @return The ShortUrl object if found
     */
    public Optional<ShortUrl> findByIdAndUser(long id, User user) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> url.getId() == id && url.getUser().getId() == user.getId())
                .findFirst();
    }

    /**
     * Видаляє короткий URL за його ідентифікатором.
     *
     * @param id Ідентифікатор URL
     * @param user Об'єкт користувача
     */
    public void deleteShortUrl(long id, User user) {
        shortUrlRepository.findAll().stream()
                .filter(url -> url.getId() == id && url.getUser().getId() == user.getId())
                .findFirst()
                .ifPresent(shortUrlRepository::delete);
    }

    /**
     * Finds the short URL by its value.
     *
     * @param shortUrl The value of the short URL
     * @return The ShortUrl object if found
     */
    public Optional<ShortUrl> findByShortUrl(String shortUrl) {
        return shortUrlRepository.findAll().stream()
                .filter(url -> url.getShortUrl().equals(shortUrl))
                .findFirst();
    }

    /**
     * Updates the ShortUrl object.
     *
     * @param shortUrl The updated ShortUrl object
     * @return The saved ShortUrl object
     */
    public ShortUrl updateShortUrl(ShortUrl shortUrl) {
        return shortUrlRepository.save(shortUrl);
    }
}
