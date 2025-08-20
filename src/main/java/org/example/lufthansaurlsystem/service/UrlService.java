package org.example.lufthansaurlsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.example.lufthansaurlsystem.configuration.JwtUtils;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final JwtUtils jwtUtils;

    @Value("${url.expiration.minutes}")
    private long defaultExpirationMinutes;

    /**
     * Shorten a long URL.
     * If the URL exists and is not expired, reset expiration.
     * If it has expired, delete it and create a new shortened URL.
     */
    public String shortenUrl(String url, String jwtToken, Long expireMinutes) {
        String username = jwtUtils.getUsernameFromToken(jwtToken);

        long effectiveExpiration = (expireMinutes != null) ? expireMinutes : defaultExpirationMinutes;
        log.info("Processing URL: {}", url);
        log.info("UrlRepository instance: {}", urlRepository);

        Optional<UrlEntity> existing = urlRepository.findByUrl(url);
        log.info("Existing URL found: {}", existing.isPresent());

        if (existing.isPresent()) {
            UrlEntity entity = existing.get();
            log.info("URL found - ID: {}, Expires: {}", entity.getId(), entity.getExpirationAt());
            if (entity.getExpirationAt().isAfter(LocalDateTime.now())) {

                log.info("URL not expired, resetting expiration for URL: {}", url);

                entity.setExpirationAt(LocalDateTime.now().plusMinutes(effectiveExpiration));
                urlRepository.save(entity);
                log.info("Returning existing short URL: {} for URL: {}", entity.getShortUrl(), url);

                return entity.getShortUrl();
            }
            log.info("URL is expired, will create new one for: {}", url);
            urlRepository.delete(entity);
        } else {
            log.info("No existing URL found, creating new one for: {}", url);
        }

        log.info("Creating new URL entity for: {}", url);
        UrlEntity entity = new UrlEntity();
        entity.setUrl(url);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(effectiveExpiration));
        entity.setClicks(0);
        entity.setUserId(username);

        String shortCode = Base62Encoder.encode(System.currentTimeMillis());
        entity.setShortUrl(shortCode);

        entity = urlRepository.save(entity);

        String properShortCode = Base62Encoder.encode(entity.getId());
        entity.setShortUrl(properShortCode);
        urlRepository.save(entity);
        log.info("Created new short URL: {} for URL: {}", properShortCode, url);
        return properShortCode;
    }

    /**
     * Resolve a short code to the original long URL.
     * Increment clicks and check expiration.
     */
    public String getLongUrl(String shortCode) {
        // Decode Base62 back to ID
        long id = Base62Encoder.decode(shortCode);

        UrlEntity entity = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // Expired check
        if (entity.getExpirationAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("URL has expired");
        }

        // Increment clicks
        entity.setClicks(entity.getClicks() + 1);
        urlRepository.save(entity);

        return entity.getUrl();

    }

    /**
     * Allow authenticated user to overwrite expiration
     * Update expiration of an existing short URL
     */
    public void updateExpiration(String shortCode, long minutes) {
        long id = Base62Encoder.decode(shortCode);

        UrlEntity entity = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        entity.setExpirationAt(LocalDateTime.now().plusMinutes(minutes));
        urlRepository.save(entity);
    }
}
