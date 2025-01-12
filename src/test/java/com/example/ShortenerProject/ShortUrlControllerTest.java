package com.example.ShortenerProject;

import com.example.ShortenerProject.shortUrl.ShortUrl;
import com.example.ShortenerProject.shortUrl.ShortUrlController;
import com.example.ShortenerProject.shortUrl.ShortUrlCreator;
import com.example.ShortenerProject.shortUrl.ShortUrlRepository;
import com.example.ShortenerProject.user.User;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShortUrlControllerTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private ShortUrlCreator shortUrlCreator;

    @InjectMocks
    private ShortUrlController shortUrlController;

    private MockMvc mockMvc;
    private final Faker faker = new Faker();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(shortUrlController).build();
    }

    @Test
    void testCreateShortUrl() throws Exception {
        // Arrange
        String originUrl = faker.internet().url();
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        when(shortUrlCreator.isValidUrl(originUrl)).thenReturn(true);
        when(shortUrlCreator.generateShortUrl()).thenReturn(shortUrl);

        ShortUrl createdShortUrl = new ShortUrl();
        createdShortUrl.setShortUrl(shortUrl);
        createdShortUrl.setOriginUrl(originUrl);
        createdShortUrl.setUser(mockUser);

        when(shortUrlRepository.save(any())).thenReturn(createdShortUrl);

        String requestBody = String.format("""
                {
                    "originUrl": "%s",
                    "dateOfExpiring": "2025-12-31T23:59:59"
                }
                """, originUrl);

        // Act and Assert
        mockMvc.perform(post("/api/v1/short-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .requestAttr("user", mockUser))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value(shortUrl))
                .andExpect(jsonPath("$.originUrl").value(originUrl));

        System.out.println("Created Short URL: " + shortUrl);
    }

    @Test
    void testGetAllShortUrls() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        ShortUrl shortUrl1 = new ShortUrl();
        shortUrl1.setShortUrl(faker.regexify("[A-Za-z0-9]{8}"));
        shortUrl1.setOriginUrl(faker.internet().url());
        shortUrl1.setUser(mockUser);

        ShortUrl shortUrl2 = new ShortUrl();
        shortUrl2.setShortUrl(faker.regexify("[A-Za-z0-9]{8}"));
        shortUrl2.setOriginUrl(faker.internet().url());
        shortUrl2.setUser(mockUser);

        List<ShortUrl> userUrls = List.of(shortUrl1, shortUrl2);
        when(shortUrlRepository.findAll()).thenReturn(userUrls);

        // Act and Assert
        mockMvc.perform(get("/api/v1/short-urls")
                        .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortUrl").value(shortUrl1.getShortUrl()))
                .andExpect(jsonPath("$[1].shortUrl").value(shortUrl2.getShortUrl()));

        System.out.println("User's Short URLs: ");
        userUrls.forEach(url -> System.out.println(" - " + url.getShortUrl()));
    }

    @Test
    void testGetShortUrlStats() throws Exception {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setShortUrl(shortUrl);
        mockShortUrl.setOriginUrl(faker.internet().url());
        mockShortUrl.setCountOfTransition(5L);
        mockShortUrl.setUser(mockUser);

        when(shortUrlRepository.findAll()).thenReturn(List.of(mockShortUrl));

        mockMvc.perform(get("/api/v1/short-urls/" + shortUrl+"/stats")
                .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(shortUrl))
                .andExpect(jsonPath("$.countOfTransition").value(5L));
    }

    @Test
    void testFindOriginalUrl() throws Exception {
        String originalUrl = faker.internet().url();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setShortUrl(faker.regexify("[A-Za-z0-9]{8}"));
        mockShortUrl.setOriginUrl(originalUrl);
        mockShortUrl.setUser(mockUser);

        when(shortUrlRepository.findAll()).thenReturn(List.of(mockShortUrl));

        mockMvc.perform(get("/api/v1/short-urls/search")
                .param("originUrl", originalUrl)
                .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(originalUrl));
    }

    @Test
    void testGetShortUrlStats_NotFound() throws Exception {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        User mockUser = new User();
        mockUser.setId(1L);

        when(shortUrlRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/short-urls/" + shortUrl + "/stats")
                        .requestAttr("user", mockUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindOriginalUrlStatus_NotFound() throws Exception
    {
        String originUrl = faker.internet().url();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setShortUrl(faker.regexify("[A-Za-z0-9]{8}"));
        mockShortUrl.setOriginUrl(originUrl);
        mockShortUrl.setUser(mockUser);

        when(shortUrlRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/short-urls/search")
                    .param("originUrl", originUrl)
                    .requestAttr("user", mockUser))
                .andExpect(status().isNotFound());
    }
}
