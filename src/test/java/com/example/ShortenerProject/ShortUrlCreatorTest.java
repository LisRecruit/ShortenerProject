package com.example.ShortenerProject;

import com.example.ShortenerProject.shortUrl.ShortUrlCreator;
import com.example.ShortenerProject.shortUrl.ShortUrlRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShortUrlCreatorTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;
    private ShortUrlCreator shortUrlCreator;
    private final Faker faker = new Faker();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        shortUrlCreator = new ShortUrlCreator(shortUrlRepository);
    }

    @Test
    void testGenerateShortUrl() {
        String shortUrl = shortUrlCreator.generateShortUrl();
        System.out.println("Generated Short URL: " + shortUrl);

        assertNotNull(shortUrl, "Short URL should not be null");
        assertEquals(8, shortUrl.length(), "Short URL should have a length of 8 characters");
    }

    @Test
    void testIsValidUrl() {
        String validUrl = faker.internet().url();
        String invalidUrl = faker.lorem().word();

        System.out.println("Testing valid URL: " + validUrl);
        System.out.println("Testing invalid URL: " + invalidUrl);

        assertTrue(shortUrlCreator.isValidUrl(validUrl), "Valid URL should return true");
        assertFalse(shortUrlCreator.isValidUrl(invalidUrl), "Invalid URL should return false");
    }

    @Test
    void testGenerateUniqueShortUrl() {
        when(shortUrlRepository.existsByShortUrl(ArgumentMatchers.anyString())).thenReturn(false);

        String uniqueShortUrl = shortUrlCreator.generateUniqueShortUrl();

        assertNotNull(uniqueShortUrl, "Unique Short URL should not be null");
        assertEquals(8, uniqueShortUrl.length(), "Unique Short URL should have a length of 8 characters");
        System.out.println("Generated Unique Short URL: " + uniqueShortUrl);

        verify(shortUrlRepository, atLeastOnce()).existsByShortUrl(ArgumentMatchers.anyString());
    }

}
