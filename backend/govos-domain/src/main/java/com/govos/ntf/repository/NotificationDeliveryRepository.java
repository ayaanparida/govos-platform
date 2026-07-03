package com.govos.ntf.repository;

import com.govos.ntf.entity.DeliveryStatus;
import com.govos.ntf.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    Optional<NotificationDelivery> findByIdAndDeletedFalse(UUID id);

    List<NotificationDelivery> findByNotification_IdAndDeletedFalseOrderByCreatedDateDesc(UUID notificationId);

    List<NotificationDelivery> findByDeliveryStatusAndDeletedFalseOrderByCreatedDateDesc(DeliveryStatus deliveryStatus);
}
