package org.example.lufthansaurlsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.example.lufthansaurlsystem.security.Base62Encoder;
import org.example.lufthansaurlsystem.security.JwtUtils;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlManagementService {

    private final UrlRepository urlRepository;
    private final JwtUtils jwtUtils;

    @Value("${url.base}")
    private String baseUrl;

    /**
     * Resolve a short code to the original URL.
     * Check expiration.
     * Increment clicks.
     */
    public String getUrlByShortCode(String shortCode) {
        UrlEntity entity = getEntityByShortCode(shortCode);

        if (entity.getExpirationAt().isBefore(LocalDateTime.now())) {
            urlRepository.delete(entity);
            log.warn("ShortCode {} has expired at {}", shortCode, entity.getExpirationAt());
            throw new RuntimeException("URL has expired");
        }

        log.info("URL {} resolved successfully. Total clicks: {}", entity.getUrl(), entity.getClicks());
        urlRepository.updateClicks(entity.getClicks() + 1, entity.getShortUrl());
        return entity.getUrl();
    }

    /**
     * Allow authenticated user to overwrite expiration
     * Update expiration of an existing short URL
     */
    public void updateExpiration(String shortCode, long minutes, String jwtToken) {
        String username = jwtUtils.getUsernameFromToken(jwtToken);

        UrlEntity entity = getEntityByShortCode(shortCode);
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(minutes));
        entity.setUserId(username);
        urlRepository.save(entity);
    }

    private UrlEntity getEntityByShortCode(String inputCode) {
        if (inputCode == null || inputCode.isBlank()) {
            throw new IllegalArgumentException("inputCode is null or blank");
        }
        log.info("Received shortCode input: {}", inputCode);

        String shortCode = inputCode.startsWith(baseUrl)
                ? inputCode.substring(baseUrl.length())
                : inputCode;
        log.info("Final shortCode to decode: {}", shortCode);

        long id = Base62Encoder.decode(shortCode);
        log.info("Decoded shortCode {} to ID {}", shortCode, id);

        return urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }
}
