package com.govos.ntf.repository;

import com.govos.ntf.entity.Notification;
import com.govos.ntf.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndDeletedFalse(UUID id);

    Optional<Notification> findByCodeAndDeletedFalse(String code);

    List<Notification> findByDeletedFalseOrderByCreatedDateDesc();

    List<Notification> findByChannel_IdAndDeletedFalseOrderByCreatedDateDesc(UUID channelId);

    List<Notification> findByStatusAndDeletedFalseOrderByCreatedDateDesc(NotificationStatus status);

    List<Notification> findByRecipientAndDeletedFalseOrderByCreatedDateDesc(String recipient);

    boolean existsByCodeAndDeletedFalse(String code);
}
