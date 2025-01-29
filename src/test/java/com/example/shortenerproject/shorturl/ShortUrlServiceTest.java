package com.example.shortenerproject.shorturl;

import com.example.shortenerproject.exception.CantBeNullException;
import com.example.shortenerproject.exception.EntityNotFoundException;
import com.example.shortenerproject.exception.InvalidOriginUrlException;
import com.example.shortenerproject.shorturl.dto.request.ShortUrlCreateRequest;
import com.example.shortenerproject.shorturl.dto.response.ShortUrlResponse;
import com.example.shortenerproject.user.User;
import com.example.shortenerproject.user.UserRepository;
import com.example.shortenerproject.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShortUrlCreator shortUrlCreator;

    @Mock
    private ShortUrlMapper shortUrlMapper;

    @Mock
    private Validator urlValidator;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ShortUrlService shortUrlService;

    private User user;
    private ShortUrlCreateRequest createRequest;
    private ShortUrl shortUrl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User(1L, "username", "Password1");
        createRequest = new ShortUrlCreateRequest("http://example.com");




        shortUrl = new ShortUrl();
        shortUrl.setOriginUrl("http://http.cat");
        shortUrl.setShortUrl("Qwerty12");
        shortUrl.setUser(user);
        shortUrl.setDateOfCreating("2025-01-20T00:00:00");
        shortUrl.setDateOfExpiring("2025-12-31T23:59:59");
        shortUrl.setCountOfTransition(0);
    }

    @Test
    void createShortUrl_ValidRequest_ShouldCreateShortUrl() {
        when(urlValidator.isValidUrl(createRequest.originUrl())).thenReturn(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(shortUrlCreator.generateUniqueShortUrl()).thenReturn("Qwerty12");
        when(shortUrlMapper.toEntity(createRequest)).thenReturn(shortUrl);
        when(shortUrlRepository.save(shortUrl)).thenReturn(shortUrl);
        when(shortUrlMapper.toResponse(shortUrl)).thenReturn(ShortUrlResponse.builder()
                .shortUrl("Qwerty12")
                .originUrl("http://http.cat")
                .dateOfCreating("2025-01-20T00:00:00")
                .dateOfExpiring("2025-12-31T23:59:59")
                .user(user.getId())
                .build());

        ShortUrlResponse response = shortUrlService.createShortUrl(createRequest, user.getId());

        assertNotNull(response);
        assertEquals("Qwerty12", response.shortUrl());
        assertEquals("http://http.cat", response.originUrl());
        assertEquals("2025-01-20T00:00:00", response.dateOfCreating());
        assertEquals("2025-12-31T23:59:59", response.dateOfExpiring());
        assertEquals(user.getId(), response.user());
    }

    @Test
    void createShortUrl_InvalidUrl_ShouldThrowException() {
        when(urlValidator.isValidUrl(createRequest.originUrl())).thenReturn(false);

        InvalidOriginUrlException exception = assertThrows(InvalidOriginUrlException.class, () ->
                shortUrlService.createShortUrl(createRequest, user.getId())
        );
        assertEquals("Invalid origin URL: http://http.cat", exception.getMessage());
    }

    @Test
    void createShortUrl_UserNotFound_ShouldThrowException() {
        when(urlValidator.isValidUrl(createRequest.originUrl())).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                shortUrlService.createShortUrl(createRequest, user.getId())
        );
        assertEquals("User with id = 1 does not exist.", exception.getMessage());
    }

    @Test
    void findShortUrlById_ExistingId_ShouldReturnShortUrlResponse() {
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));

        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(ShortUrlResponse.builder()
                        .shortUrl("short.ly/example")
                        .originUrl("http://example.com")
                        .dateOfCreating("2025-01-20T00:00:00")
                        .dateOfExpiring("2025-12-31T23:59:59")
                        .user(1L)
                        .build());
        ShortUrlResponse response = shortUrlService.findShortUrlById(1L);

        assertNotNull(response);
        assertEquals("short.ly/example", response.shortUrl());
        assertEquals("http://example.com", response.originUrl());
        assertEquals("2025-01-20T00:00:00", response.dateOfCreating());
        assertEquals("2025-12-31T23:59:59", response.dateOfExpiring());
        assertEquals(1L, response.user());
    }

    @Test
    void findShortUrlById_NotFound_ShouldThrowException() {
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                shortUrlService.findShortUrlById(1L));
        assertEquals("Short URL with ID 1 does not exist.", exception.getMessage());
    }

    @Test
    void findByUser_ValidUser_ShouldReturnShortUrls() {
        when(shortUrlRepository.findByUserId(user.getId())).thenReturn(List.of(shortUrl));
        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(ShortUrlResponse.builder()
                        .shortUrl("short.ly/example")
                        .originUrl("http://example.com")
                        .dateOfCreating("2025-01-20T00:00:00")
                        .dateOfExpiring("2025-12-31T23:59:59")
                        .user(user.getId())
                        .build());
        List<ShortUrlResponse> responses = shortUrlService.findByUser(user);
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("short.ly/example", responses.get(0).shortUrl());
        assertEquals("http://example.com", responses.get(0).originUrl());
        assertEquals("2025-01-20T00:00:00", responses.get(0).dateOfCreating());
        assertEquals("2025-12-31T23:59:59", responses.get(0).dateOfExpiring());
        assertEquals(user.getId(), responses.get(0).user());
    }

    @Test
    void findByUser_UserIsNull_ShouldThrowCantBeNullException() {
        CantBeNullException exception = assertThrows(CantBeNullException.class, () -> shortUrlService.findByUser(null));
        assertEquals("User can not be null", exception.getMessage());
    }

    @Test
    void updateShortUrl_ValidRequest_ShouldUpdateShortUrl() {
        ShortUrlCreateRequest updateRequest = new ShortUrlCreateRequest("http://updated-example.com");


        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrlCreator.generateUniqueShortUrl()).thenReturn("short.ly/updated");
        when(shortUrlRepository.save(shortUrl)).thenReturn(shortUrl);

        when(shortUrlMapper.toResponse(shortUrl))
                .thenReturn(ShortUrlResponse.builder()
                        .shortUrl("short.ly/updated")
                        .originUrl("http://updated-example.com")
                        .dateOfCreating("2025-01-20T00:00:00")
                        .dateOfExpiring("2025-12-31T23:59:59")
                        .user(1L)
                        .build());

        ShortUrlResponse response = shortUrlService.updateShortUrl(updateRequest, 1L, user);

        assertNotNull(response);
        assertEquals("short.ly/updated", response.shortUrl());
        assertEquals("http://updated-example.com", response.originUrl());
        assertEquals("2025-01-20T00:00:00", response.dateOfCreating());
        assertEquals("2025-12-31T23:59:59", response.dateOfExpiring());
        assertEquals(1L, response.user());
    }

    @Test
    void updateShortUrl_UserNotFound_ShouldThrowException() {
        ShortUrlCreateRequest updateRequest = new ShortUrlCreateRequest("http://updated-example.com" );

        when(shortUrlRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                shortUrlService.updateShortUrl(updateRequest, 1L, user)
        );

        assertEquals("Short URL with ID 1 does not exist or does not belong to the user.",
                exception.getMessage());
    }

    @Test
    void deleteShortUrl_ValidId_ShouldDeleteShortUrl() {
        doNothing().when(shortUrlRepository).deleteById(1L);

        shortUrlService.deleteShortUrl(1L);

        verify(shortUrlRepository).deleteById(1L);
    }

    @Test
    void deleteShortUrl_IdNotFound_ShouldThrowException() {
        doThrow(new EntityNotFoundException("Short URL not found")).when(shortUrlRepository).deleteById(1L);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                shortUrlService.deleteShortUrl(1L)
        );
        assertEquals("Short URL not found", exception.getMessage());
    }
}