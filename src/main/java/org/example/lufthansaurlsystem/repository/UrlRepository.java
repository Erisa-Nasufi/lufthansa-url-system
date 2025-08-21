package org.example.lufthansaurlsystem.repository;

import org.example.lufthansaurlsystem.Entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByUrl(String url);

    @Query("update UrlEntity u set u.clicks = :clicks where u.url = :url")
    @Modifying
    void updateClicksByUrl(int clicks, String url);
}
