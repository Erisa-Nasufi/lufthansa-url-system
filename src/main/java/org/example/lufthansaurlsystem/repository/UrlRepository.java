package org.example.lufthansaurlsystem.repository;

import jakarta.transaction.Transactional;
import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByUrl(String url);

    @Modifying
    @Transactional
    @Query(value = "update url set clicks = :clicks where short_url = :url", nativeQuery = true)
    void updateClicks(@Param("clicks") int clicks, @Param("url")  String url);
}
