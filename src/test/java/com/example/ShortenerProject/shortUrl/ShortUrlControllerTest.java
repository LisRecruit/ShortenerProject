package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlStatsResponse;
import com.example.ShortenerProject.user.User;
import com.example.ShortenerProject.user.UserRepository;
import com.example.ShortenerProject.utils.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShortUrlControllerTest {

    @Mock
    private ShortUrlService shortUrlService;

    @Mock
    private ShortUrlCreator shortUrlCreator;
    @Mock
    private Validator validator;
    @Mock
    private UserRepository userRepository;


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
        String originUrl = "https://http.cat";
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(faker.name().firstName());

        when(validator.isValidUrl(originUrl)).thenReturn(true);
        when(shortUrlCreator.generateUniqueShortUrl()).thenReturn(shortUrl);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(mockUser));

        ShortUrlCreateRequest request = new ShortUrlCreateRequest();
        request.setOriginUrl(originUrl);
        request.setUser(userId);
        request.setDateOfExpiring("2025-12-31T23:59:59");

        ShortUrlResponse createdShortUrlResponse = ShortUrlResponse.builder()
                .shortUrl(shortUrl)
                .originUrl(originUrl)
                .dateOfCreating("2025-01-17T00:00:00")
                .dateOfExpiring("2025-12-31T23:59:59")
                .user(userId)
                .build();
        when(shortUrlService.createShortUrl(any(ShortUrlCreateRequest.class))).thenReturn(createdShortUrlResponse);

        String requestBody = new ObjectMapper().writeValueAsString(request);

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
        User testUser = User.builder()
                .id(1L)
                .username("test_user")
                .password("secure_password")
                .build();
        when(shortUrlService.findAllShortUrlsByUser(testUser)).thenReturn(List.of(
                ShortUrlResponse.builder()
                        .shortUrl("QWertY14")
                        .originUrl("https://example.com")
                        .dateOfCreating("2025-01-17T00:00:00")
                        .dateOfExpiring("2025-12-31T23:59:59")
                        .user(1L)
                        .build()
        ));
        mockMvc.perform(get("/api/v1/short-urls")
                        .requestAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortUrl").value("QWertY14"))
                .andExpect(jsonPath("$[0].originUrl").value("https://example.com"))
                .andExpect(jsonPath("$[0].dateOfCreating").value("2025-01-17T00:00:00"))
                .andExpect(jsonPath("$[0].dateOfExpiring").value("2025-12-31T23:59:59"))
                .andExpect(jsonPath("$[0].user").value(1L));
    }

    @Test
    void testGetShortUrlStats() throws Exception {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        long countOfTransition = 5L;
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().firstName());

        ShortUrlStatsResponse mockShortUrlStatsResponse = new ShortUrlStatsResponse(shortUrl, countOfTransition);

        when(shortUrlService.getShortUrlStats(shortUrl, mockUser))
                .thenReturn(Optional.of(mockShortUrlStatsResponse));

        mockMvc.perform(get("/api/v1/short-urls/" + shortUrl + "/stats")
                        .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(shortUrl))
                .andExpect(jsonPath("$.countOfTransition").value(countOfTransition));
    }

    @Test
    void testFindOriginalUrl() throws Exception {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        String originalUrl = faker.internet().url();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().firstName());

        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setShortUrl(shortUrl);
        mockShortUrl.setOriginUrl(originalUrl);
        mockShortUrl.setUser(mockUser);

        when(shortUrlService.findOriginalUrl(shortUrl, mockUser)).thenReturn(Optional.of(originalUrl));

        mockMvc.perform(get("/api/v1/short-urls/search")
                        .param("shortUrl", shortUrl)
                        .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(originalUrl));
    }

    @Test
    void testGetShortUrlStats_NotFound() throws Exception {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        User mockUser = new User();
        mockUser.setId(1L);

        when(shortUrlService.findAllShortUrls()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/short-urls/" + shortUrl + "/stats")
                        .requestAttr("user", mockUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindOriginalUrlStatus_NotFound() throws Exception
    {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().firstName());


        when(shortUrlService.findOriginalUrl(shortUrl, mockUser)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/short-urls/search")
                        .param("shortUrl", shortUrl)
                        .requestAttr("user", mockUser))
                .andExpect(status().isNotFound());
    }
}