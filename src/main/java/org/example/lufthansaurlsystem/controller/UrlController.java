package org.example.lufthansaurlsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lufthansaurlsystem.service.UrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;
    @Value("${url.base}")
    private String baseUrl;

    /**
     * Shorten a URL.
     * JWT token must be in Authorization header: "Bearer <token>"
     */
    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestParam String url,
                                             @RequestParam(required = false) Long expireMinutes,
                                             @RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.replace("Bearer ", "");
        log.info("Extracted JWT token: {}", jwtToken.substring(0, Math.min(jwtToken.length(), 20)) + "...");

        String shortCode = urlService.shortenUrl(url, jwtToken, expireMinutes);
        String shortUrl = baseUrl + shortCode;

        log.info("Generated short URL: {}", shortUrl);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> getLongUrl(@PathVariable String shortCode) {
        String longUrl = urlService.getLongUrl(shortCode);
        return ResponseEntity.ok(longUrl);
    }

    /**
     * Overwrite expiration for a given short URL.
     * JWT token required for authentication.
     */
    @PutMapping("/{shortCode}/expiration")
    public ResponseEntity<String> updateExpiration(@PathVariable String shortCode,
                                                   @RequestParam long minutes) {
        urlService.updateExpiration(shortCode, minutes);
        return ResponseEntity.ok("Expiration updated successfully");
    }
}