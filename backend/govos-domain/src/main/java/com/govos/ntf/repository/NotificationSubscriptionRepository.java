package com.govos.ntf.repository;

import com.govos.ntf.entity.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, UUID> {

    Optional<NotificationSubscription> findByIdAndDeletedFalse(UUID id);

    List<NotificationSubscription> findByUser_IdAndDeletedFalse(UUID userId);

    List<NotificationSubscription> findByEventTypeAndDeletedFalse(String eventType);

    boolean existsByUser_IdAndEventTypeAndChannel_IdAndDeletedFalse(
            UUID userId, String eventType, UUID channelId);
}
