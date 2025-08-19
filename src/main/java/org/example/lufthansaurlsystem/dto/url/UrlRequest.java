package org.example.lufthansaurlsystem.dto.url;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@AllArgsConstructor
@Data
public class UrlRequest {
    private String longUrl;
    private Duration expiration;
}
