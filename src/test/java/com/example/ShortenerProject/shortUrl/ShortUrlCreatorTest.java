package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.utils.Validator;
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
    private Validator validator;
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
    void testGenerateUniqueShortUrl() {
        when(shortUrlRepository.existsByShortUrl(ArgumentMatchers.anyString())).thenReturn(false);

        String uniqueShortUrl = shortUrlCreator.generateUniqueShortUrl();

        assertNotNull(uniqueShortUrl, "Unique Short URL should not be null");
        assertEquals(8, uniqueShortUrl.length(), "Unique Short URL should have a length of 8 characters");
        System.out.println("Generated Unique Short URL: " + uniqueShortUrl);

        verify(shortUrlRepository, atLeastOnce()).existsByShortUrl(ArgumentMatchers.anyString());
    }

}
