package com.example.ShortenerProject;

import com.example.ShortenerProject.shortUrl.ShortUrlCreator;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShortUrlCreatorTest {

    private final ShortUrlCreator shortUrlCreator = new ShortUrlCreator();
    private final Faker faker = new Faker();

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

}
