package com.govos.doc.repository;

import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.StorageProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorageProviderRepository extends JpaRepository<StorageProvider, UUID> {

    Optional<StorageProvider> findByIdAndDeletedFalse(UUID id);

    Optional<StorageProvider> findByProviderNameAndDeletedFalse(String providerName);

    List<StorageProvider> findByProviderTypeAndDeletedFalse(StorageProviderType providerType);

    List<StorageProvider> findByActiveTrueAndDeletedFalse();

    Optional<StorageProvider> findByIsDefaultTrueAndDeletedFalse();
}
