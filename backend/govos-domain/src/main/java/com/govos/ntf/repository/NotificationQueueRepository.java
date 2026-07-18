package com.govos.ntf.repository;

import com.govos.ntf.entity.NotificationQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, UUID> {

    Optional<NotificationQueue> findByIdAndDeletedFalse(UUID id);

    List<NotificationQueue> findByNotification_IdAndDeletedFalse(UUID notificationId);

    Optional<NotificationQueue> findByNotification_IdAndDeletedFalseAndActiveTrue(UUID notificationId);
}
