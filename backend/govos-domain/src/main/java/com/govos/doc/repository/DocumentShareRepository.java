package com.govos.doc.repository;

import com.govos.doc.entity.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, UUID> {

    Optional<DocumentShare> findByIdAndDeletedFalse(UUID id);

    List<DocumentShare> findByDocument_IdAndDeletedFalse(UUID documentId);

    List<DocumentShare> findBySharedWithUserIdAndDeletedFalse(UUID recipientUserId);

    Optional<DocumentShare> findByShareToken_TokenHashAndDeletedFalse(String tokenHash);

    List<DocumentShare> findByExpiresAtAfterAndDeletedFalse(Instant expiryDate);
}
