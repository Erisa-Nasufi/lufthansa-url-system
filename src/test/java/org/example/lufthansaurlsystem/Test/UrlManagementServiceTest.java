package org.example.lufthansaurlsystem.Test;

import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.example.lufthansaurlsystem.security.JwtUtils;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.example.lufthansaurlsystem.service.UrlManagementService;
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
class UrlManagementServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UrlManagementService urlManagementService;

    private static final String TEST_SHORT_CODE = "abc123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_JWT_TOKEN = "test.jwt.token";
    private static final String TEST_ORIGINAL_URL = "https://www.example.com/very/long/url";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlManagementService, "baseUrl", "https://short.url/");
    }

    @Test
    @DisplayName("Should return original URL when short code is valid and not expired")
    void shouldReturnOriginalUrlWhenShortCodeIsValidAndNotExpired() {
        UrlEntity entity = new UrlEntity();
        entity.setId(1L);
        entity.setUrl(TEST_ORIGINAL_URL);
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(10));
        entity.setClicks(5);
        entity.setShortUrl("shortUrl123");

        when(urlRepository.findById(any(Long.class))).thenReturn(Optional.of(entity));
        doNothing().when(urlRepository).updateClicksByUrl(any(Integer.class), any(String.class));


        String result = urlManagementService.getUrlByShortCode(TEST_SHORT_CODE);

        assertEquals(TEST_ORIGINAL_URL, result);
        verify(urlRepository).updateClicksByUrl(6, entity.getShortUrl());
    }

    @Test
    @DisplayName("Should throw exception when URL is expired")
    void shouldThrowExceptionWhenUrlIsExpired() {
        UrlEntity expiredEntity = new UrlEntity();
        expiredEntity.setId(1L);
        expiredEntity.setUrl(TEST_ORIGINAL_URL);
        expiredEntity.setExpirationAt(LocalDateTime.now().minusMinutes(5));

        when(urlRepository.findById(any(Long.class))).thenReturn(Optional.of(expiredEntity));

        assertThrows(RuntimeException.class, () -> {
            urlManagementService.getUrlByShortCode(TEST_SHORT_CODE);
        });

        verify(urlRepository).delete(expiredEntity);
    }

    @Test
    @DisplayName("Should update expiration time successfully")
    void shouldUpdateExpirationTimeSuccessfully() {
        UrlEntity entity = new UrlEntity();
        entity.setId(1L);
        entity.setUrl(TEST_ORIGINAL_URL);
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(5));

        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findById(any(Long.class))).thenReturn(Optional.of(entity));
        when(urlRepository.save(any(UrlEntity.class))).thenReturn(entity);

        long newExpirationMinutes = 60L;

        assertDoesNotThrow(() -> {
            urlManagementService.updateExpiration(TEST_SHORT_CODE, newExpirationMinutes, TEST_JWT_TOKEN);
        });

        verify(urlRepository).save(entity);
        assertTrue(entity.getExpirationAt().isAfter(LocalDateTime.now().plusMinutes(55)));
    }

    @Test
    @DisplayName("Should handle short code with base URL prefix")
    void shouldHandleShortCodeWithBaseUrlPrefix() {
        String shortCodeWithPrefix = "https://short.url/" + TEST_SHORT_CODE;
        UrlEntity entity = new UrlEntity();
        entity.setId(1L);
        entity.setUrl(TEST_ORIGINAL_URL);
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(10));
        entity.setShortUrl("shortUrl123");

        when(urlRepository.findById(any(Long.class))).thenReturn(Optional.of(entity));
        doNothing().when(urlRepository).updateClicksByUrl(any(Integer.class), any(String.class));

        String result = urlManagementService.getUrlByShortCode(shortCodeWithPrefix);

        assertEquals(TEST_ORIGINAL_URL, result);
    }

    @Test
    @DisplayName("Should throw exception for null or blank short code")
    void shouldThrowExceptionForNullOrBlankShortCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            urlManagementService.getUrlByShortCode(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            urlManagementService.getUrlByShortCode("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            urlManagementService.getUrlByShortCode("   ");
        });
    }

    @Test
    @DisplayName("Should throw exception when URL not found")
    void shouldThrowExceptionWhenURLNotFound() {
        when(urlRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            urlManagementService.getUrlByShortCode(TEST_SHORT_CODE);
        });
    }

    @Test
    @DisplayName("Should extract username from JWT token")
    void shouldExtractUsernameFromJwtToken() {
        UrlEntity entity = new UrlEntity();
        entity.setId(1L);
        entity.setUrl(TEST_ORIGINAL_URL);
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(10));

        when(jwtUtils.getUsernameFromToken(TEST_JWT_TOKEN)).thenReturn(TEST_USERNAME);
        when(urlRepository.findById(any(Long.class))).thenReturn(Optional.of(entity));
        when(urlRepository.save(any(UrlEntity.class))).thenReturn(entity);

        urlManagementService.updateExpiration(TEST_SHORT_CODE, 60L, TEST_JWT_TOKEN);

        verify(jwtUtils).getUsernameFromToken(TEST_JWT_TOKEN);
    }
}