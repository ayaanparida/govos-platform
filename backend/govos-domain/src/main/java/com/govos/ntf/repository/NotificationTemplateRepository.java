package com.govos.ntf.repository;

import com.govos.ntf.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByIdAndDeletedFalse(UUID id);

    Optional<NotificationTemplate> findByCodeAndDeletedFalse(String code);

    List<NotificationTemplate> findByDeletedFalseOrderByNameAsc();

    List<NotificationTemplate> findByChannel_IdAndDeletedFalseOrderByNameAsc(UUID channelId);

    boolean existsByCodeAndDeletedFalse(String code);
}
