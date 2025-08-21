package org.example.lufthansaurlsystem.Test;

import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.example.lufthansaurlsystem.repository.UrlRepository;
import org.example.lufthansaurlsystem.cron.UrlCronJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class urlCronJobTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlCronJob UrlCronJob;

    @Test
    @DisplayName("Should delete expired URLs during cleanup")
    void shouldDeleteExpiredUrlsDuringCleanup() {

        UrlEntity expiredUrl1 = new UrlEntity();
        expiredUrl1.setId(1L);
        expiredUrl1.setExpirationAt(LocalDateTime.now().minusMinutes(10));

        UrlEntity expiredUrl2 = new UrlEntity();
        expiredUrl2.setId(2L);
        expiredUrl2.setExpirationAt(LocalDateTime.now().minusMinutes(5));

        UrlEntity validUrl = new UrlEntity();
        validUrl.setId(3L);
        validUrl.setExpirationAt(LocalDateTime.now().plusMinutes(10));

        List<UrlEntity> allUrls = Arrays.asList(expiredUrl1, expiredUrl2, validUrl);

        when(urlRepository.findAll()).thenReturn(allUrls);


        UrlCronJob.cleanExpiredUrls();


        verify(urlRepository).delete(expiredUrl1);
        verify(urlRepository).delete(expiredUrl2);
        verify(urlRepository, never()).delete(validUrl);
    }

    @Test
    @DisplayName("Should handle empty URL list during cleanup")
    void shouldHandleEmptyUrlListDuringCleanup() {

        when(urlRepository.findAll()).thenReturn(Arrays.asList());


        assertDoesNotThrow(() -> {
            UrlCronJob.cleanExpiredUrls();
        });


        verify(urlRepository, never()).delete(any(UrlEntity.class));
    }

    @Test
    @DisplayName("Should handle all valid URLs during cleanup")
    void shouldHandleAllValidUrlsDuringCleanup() {

        UrlEntity validUrl1 = new UrlEntity();
        validUrl1.setId(1L);
        validUrl1.setExpirationAt(LocalDateTime.now().plusMinutes(10));

        UrlEntity validUrl2 = new UrlEntity();
        validUrl2.setId(2L);
        validUrl2.setExpirationAt(LocalDateTime.now().plusMinutes(20));

        List<UrlEntity> allValidUrls = Arrays.asList(validUrl1, validUrl2);

        when(urlRepository.findAll()).thenReturn(allValidUrls);


        UrlCronJob.cleanExpiredUrls();


        verify(urlRepository, never()).delete(any(UrlEntity.class));
    }
}