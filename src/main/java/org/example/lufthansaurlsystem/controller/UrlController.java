package org.example.lufthansaurlsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lufthansaurlsystem.service.UrlManagementService;
import org.example.lufthansaurlsystem.service.UrlShorteningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlManagementService urlManagementService;
    private final UrlShorteningService urlShorteningService;
    @Value("${url.base}")
    private String baseUrl;

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestParam String url,
                                             @RequestParam(required = false) Long expireMinutes,
                                             @RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.replace("Bearer ", "");
        log.info("Extracted JWT token: {}", jwtToken.substring(0, Math.min(jwtToken.length(), 20)) + "...");

        String shortCode = urlShorteningService.shortenUrl(url, jwtToken, expireMinutes);
        String shortUrl = baseUrl + shortCode;

        log.info("Generated short URL: {}", shortUrl);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> getUrl(@PathVariable String shortCode,
                                         @RequestHeader("Authorization") String authHeader) {
        String url = urlManagementService.getUrlByShortCode(shortCode);
        return ResponseEntity.ok(url);
    }

    @PutMapping("/{shortCode}/expiration")
    public ResponseEntity<String> updateExpiration(@PathVariable String shortCode,
                                                   @RequestParam long minutes,
                                                   @RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.replace("Bearer ", "");
        urlManagementService.updateExpiration(shortCode, minutes, jwtToken);
        return ResponseEntity.ok("Expiration updated successfully");
    }
}