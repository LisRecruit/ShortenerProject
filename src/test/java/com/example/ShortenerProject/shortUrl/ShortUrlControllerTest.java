package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.user.User;
import com.example.ShortenerProject.user.UserRepository;
import com.example.ShortenerProject.utils.Validator;
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
        // Arrange
        String originUrl = faker.internet().url();
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(faker.name().username());


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

        ShortUrlResponse response1 = new ShortUrlResponse(
                shortUrl1.getShortUrl(),
                shortUrl1.getOriginUrl(),
                shortUrl1.getDateOfCreating(),
                shortUrl1.getDateOfExpiring(),
                shortUrl1.getUser().getId()
        );
        ShortUrlResponse response2 = new ShortUrlResponse(
                shortUrl2.getShortUrl(),
                shortUrl2.getOriginUrl(),
                shortUrl2.getDateOfCreating(),
                shortUrl2.getDateOfExpiring(),
                shortUrl2.getUser().getId()
        );

        List<ShortUrlResponse> userUrls = List.of(response1, response2);
        when(shortUrlService.findAllShortUrls()).thenReturn(userUrls);

        // Act and Assert
        mockMvc.perform(get("/api/v1/short-urls")
                        .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortUrl").value(response1.shortUrl()))
                .andExpect(jsonPath("$[1].shortUrl").value(response2.shortUrl()));

        System.out.println("User's Short URLs: ");
        userUrls.forEach(url -> System.out.println(" - " + url.shortUrl()));
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

        ShortUrlResponse mockShortUrlResponse = new ShortUrlResponse(
                shortUrl,
                mockShortUrl.getOriginUrl(),
                mockShortUrl.getDateOfCreating(),
                mockShortUrl.getDateOfExpiring(),
                mockUser.getId()
        );

        when(shortUrlService.findAllShortUrls()).thenReturn(List.of(mockShortUrlResponse));

        mockMvc.perform(get("/api/v1/short-urls/" + shortUrl+"/stats")
                        .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(shortUrl))
                .andExpect(jsonPath("$.countOfTransition").value(5L));
    }

    @Test
    void testFindOriginalUrl() throws Exception {
        String shortUrl = faker.regexify("[A-Za-z0-9]{8}");
        String originalUrl = faker.internet().url();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setShortUrl(faker.regexify("[A-Za-z0-9]{8}"));
        mockShortUrl.setOriginUrl(originalUrl);
        mockShortUrl.setUser(mockUser);

        ShortUrlResponse mockShortUrlResponse = new ShortUrlResponse(
                shortUrl,
                mockShortUrl.getOriginUrl(),
                mockShortUrl.getDateOfCreating(),
                mockShortUrl.getDateOfExpiring(),
                mockUser.getId()
        );
        when(shortUrlService.findAllShortUrls()).thenReturn(List.of(mockShortUrlResponse));

        mockMvc.perform(get("/api/v1/short-urls/search")
                        .param("originUrl", originalUrl)
                        .requestAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(mockShortUrl.getShortUrl()));
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
        String originUrl = faker.internet().url();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(faker.name().username());

        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setShortUrl(faker.regexify("[A-Za-z0-9]{8}"));
        mockShortUrl.setOriginUrl(originUrl);
        mockShortUrl.setUser(mockUser);

        when(shortUrlService.findAllShortUrls()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/short-urls/search")
                        .param("originUrl", originUrl)
                        .requestAttr("user", mockUser))
                .andExpect(status().isNotFound());
    }
}