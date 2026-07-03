package com.govos.idm.repository;

import com.govos.idm.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    Optional<PasswordHistory> findByIdAndDeletedFalse(UUID id);

    List<PasswordHistory> findByUser_IdOrderByChangedDateDesc(UUID userId);
}
