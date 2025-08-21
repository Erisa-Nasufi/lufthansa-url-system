package org.example.lufthansaurlsystem.cron;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class UrlCronJob {

    private final UrlRepository urlRepository;

//    @Scheduled(cron = "0 * * * * *")
    public void cleanExpiredUrls() {
        log.info("Scheduled task started at: {}", LocalDateTime.now());

        try {
            long expiredCount = urlRepository.findAll().stream()
                    .filter(url -> url.getExpirationAt().isBefore(LocalDateTime.now()))
                    .peek(url -> log.info("Found expired URL: {} (expired at: {})",
                            url.getShortUrl(), url.getExpirationAt()))
                    .count();

            if (expiredCount > 0) {
                urlRepository.findAll().stream()
                        .filter(url -> url.getExpirationAt().isBefore(LocalDateTime.now()))
                        .forEach(url -> {
                            log.info("🗑Deleting expired URL: {}", url.getShortUrl());
                            urlRepository.delete(url);
                        });
                log.info("Cleanup completed. Deleted {} expired URLs", expiredCount);
            } else {
                log.info("No expired URLs found to delete");
            }
        } catch (Exception e) {
            log.error(" Error during cleanup: {}", e.getMessage(), e);
        }
    }
}
