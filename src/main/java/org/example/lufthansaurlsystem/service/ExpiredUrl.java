package org.example.lufthansaurlsystem.service;

import lombok.AllArgsConstructor;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ExpiredUrl {

    private final UrlRepository urlRepository;

    @Scheduled(cron = "0 * * * * *")
    public void cleanExpiredUrls() {
        urlRepository.findAll().stream()
                .filter(url -> url.getExpirationAt().isBefore(LocalDateTime.now()))
                .forEach(urlRepository::delete);
    }
}
