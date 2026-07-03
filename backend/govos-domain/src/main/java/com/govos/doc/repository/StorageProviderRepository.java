package com.govos.doc.repository;

import com.govos.doc.entity.StorageProvider;
import com.govos.doc.entity.StorageProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorageProviderRepository extends JpaRepository<StorageProvider, UUID> {

    Optional<StorageProvider> findByIdAndDeletedFalse(UUID id);

    Optional<StorageProvider> findByCodeAndDeletedFalse(String code);

    List<StorageProvider> findByDeletedFalseOrderByCodeAsc();

    List<StorageProvider> findByProviderTypeAndDeletedFalseOrderByCodeAsc(StorageProviderType providerType);

    boolean existsByCodeAndDeletedFalse(String code);
}
