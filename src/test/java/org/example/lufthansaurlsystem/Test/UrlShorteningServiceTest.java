package org.example.lufthansaurlsystem.Test;

import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.example.lufthansaurlsystem.security.JwtUtils;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.example.lufthansaurlsystem.service.UrlShorteningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UrlShorteningService urlShorteningService;

    private static final String TEST_URL = "https://www.example.com/very/long/url/that/needs/shortening";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_JWT_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlShorteningService, "defaultExpirationMinutes", 5L);
        ReflectionTestUtils.setField(urlShorteningService, "baseUrl", "https://short.url/");
    }

    @Test
    @DisplayName("Should create new short URL when URL does not exist")
    void shouldCreateNewShortUrlWhenUrlDoesNotExist() {
        // Arrange
        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findByUrl(TEST_URL)).thenReturn(Optional.empty());
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> {
            UrlEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // Act
        String result = urlShorteningService.shortenUrl(TEST_URL, TEST_JWT_TOKEN, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.length() > 0);
        verify(urlRepository, times(2)).save(any(UrlEntity.class));
        verify(urlRepository).findByUrl(TEST_URL);
    }

    @Test
    @DisplayName("Should return existing short URL when URL exists and is not expired")
    void shouldReturnExistingShortUrlWhenUrlExistsAndNotExpired() {
        // Arrange
        UrlEntity existingEntity = new UrlEntity();
        existingEntity.setId(1L);
        existingEntity.setUrl(TEST_URL);
        existingEntity.setExpirationAt(LocalDateTime.now().plusMinutes(10));
        existingEntity.setShortUrl("existingShortCode");

        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findByUrl(TEST_URL)).thenReturn(Optional.of(existingEntity));
        when(urlRepository.save(any(UrlEntity.class))).thenReturn(existingEntity);

        // Act
        String result = urlShorteningService.shortenUrl(TEST_URL, TEST_JWT_TOKEN, null);

        // Assert
        assertNotNull(result);
        verify(urlRepository).save(existingEntity);
        verify(urlRepository, never()).delete(any(UrlEntity.class));
    }

    @Test
    @DisplayName("Should create new short URL when existing URL is expired")
    void shouldCreateNewShortUrlWhenExistingUrlIsExpired() {
        // Arrange
        UrlEntity expiredEntity = new UrlEntity();
        expiredEntity.setId(1L);
        expiredEntity.setUrl(TEST_URL);
        expiredEntity.setExpirationAt(LocalDateTime.now().minusMinutes(5));
        expiredEntity.setShortUrl("expiredShortCode");

        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findByUrl(TEST_URL)).thenReturn(Optional.of(expiredEntity));
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> {
            UrlEntity entity = invocation.getArgument(0);
            entity.setId(2L);
            return entity;
        });

        // Act
        String result = urlShorteningService.shortenUrl(TEST_URL, TEST_JWT_TOKEN, null);

        // Assert
        assertNotNull(result);
        verify(urlRepository).delete(expiredEntity);
        verify(urlRepository, times(2)).save(any(UrlEntity.class));
    }

    @Test
    @DisplayName("Should use custom expiration time when provided")
    void shouldUseCustomExpirationTimeWhenProvided() {
        // Arrange
        Long customExpiration = 60L; // 60 minutes
        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findByUrl(TEST_URL)).thenReturn(Optional.empty());
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> {
            UrlEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // Act
        String result = urlShorteningService.shortenUrl(TEST_URL, TEST_JWT_TOKEN, customExpiration);

        // Assert
        assertNotNull(result);
        verify(urlRepository, times(2)).save(any(UrlEntity.class));
    }

    @Test
    @DisplayName("Should extract username from JWT token")
    void shouldExtractUsernameFromJwtToken() {
        // Arrange
        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findByUrl(TEST_URL)).thenReturn(Optional.empty());
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(invocation -> {
            UrlEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // Act
        urlShorteningService.shortenUrl(TEST_URL, TEST_JWT_TOKEN, null);

        // Assert
        verify(jwtUtils).getUsernameFromToken(TEST_JWT_TOKEN);
    }
}