package com.gamegrind.dev.AuthApplication.repositories;

import com.gamegrind.dev.AuthApplication.entities.RefreshToken;
import com.gamegrind.dev.AuthApplication.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJti(String jti);
    List<RefreshToken> findAllByUser(User user);

    @Modifying
    @Transactional
    void deleteAllByUser(User user);
}
