package com.govos.ntf.repository;

import com.govos.ntf.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByIdAndDeletedFalse(UUID id);

    List<NotificationPreference> findByUser_IdAndDeletedFalse(UUID userId);

    Optional<NotificationPreference> findByUser_IdAndChannel_IdAndDeletedFalse(UUID userId, UUID channelId);

    boolean existsByUser_IdAndChannel_IdAndDeletedFalse(UUID userId, UUID channelId);
}
