package org.example.lufthansaurlsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.example.lufthansaurlsystem.configuration.JwtUtils;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
     */
    public String shortenUrl(String url, String jwtToken, Long expireMinutes) {
        String username = jwtUtils.getUsernameFromToken(jwtToken);

        // Decide expiration time: user value OR default
        long effectiveExpiration = (expireMinutes != null) ? expireMinutes : defaultExpirationMinutes;

        Optional<UrlEntity> existing = urlRepository.findByUrl(url);

        if (existing.isPresent()) {
            UrlEntity entity = existing.get();


            // If not expired, return same short code but reset expiration
            if (entity.getExpirationAt().isAfter(LocalDateTime.now())) {
                // Reset expiration if not expired
                entity.setExpirationAt(LocalDateTime.now().plusMinutes(effectiveExpiration));
                urlRepository.save(entity);
                return entity.getShortUrl();
            }
        }

        // Create new URL entity
        UrlEntity entity = new UrlEntity();
        entity.setUrl(url);
        entity.setExpirationAt(LocalDateTime.now().plusMinutes(effectiveExpiration));
        entity.setClicks(0);
        entity.setUserId(username); // associate with authenticated user
        urlRepository.save(entity);

        String shortCode = Base62Encoder.encode(entity.getId());
        entity.setShortUrl(shortCode);
        urlRepository.save(entity);

        return shortCode;
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
