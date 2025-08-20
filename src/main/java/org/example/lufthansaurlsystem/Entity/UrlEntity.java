package org.example.lufthansaurlsystem.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "URL")
public class UrlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false, unique = true)
    private String shortUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expiration_at")
    private LocalDateTime expirationAt;

    @Column(name = "clicks")
    private int clicks;

    @Column(name = "user_id")
    private String userId;
}
