package com.govos.idm.repository;

import com.govos.idm.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByIdAndDeletedFalse(UUID id);

    Optional<RefreshToken> findByTokenAndDeletedFalseAndRevokedFalse(String token);

    List<RefreshToken> findByUser_IdAndDeletedFalseAndRevokedFalse(UUID userId);
}
