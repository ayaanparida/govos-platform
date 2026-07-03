package com.govos.idm.repository;

import com.govos.idm.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    Optional<LoginHistory> findByIdAndDeletedFalse(UUID id);

    List<LoginHistory> findByUser_IdOrderByLoginTimeDesc(UUID userId);
}
