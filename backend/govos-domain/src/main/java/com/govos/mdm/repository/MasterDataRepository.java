package com.govos.mdm.repository;

import com.govos.mdm.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, UUID> {

    List<MasterData> findByTypeAndDeletedFalseOrderByDisplayOrderAsc(String type);

    Optional<MasterData> findByIdAndDeletedFalse(UUID id);

    Optional<MasterData> findByTypeAndKeyAndDeletedFalse(String type, String key);

    boolean existsByTypeAndKeyAndDeletedFalse(String type, String key);
}
