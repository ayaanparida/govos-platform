package com.govos.ntf.repository;

import com.govos.ntf.entity.ChannelProvider;
import com.govos.ntf.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, UUID> {

    Optional<NotificationChannel> findByIdAndDeletedFalse(UUID id);

    Optional<NotificationChannel> findByCodeAndDeletedFalse(String code);

    List<NotificationChannel> findByDeletedFalseOrderByNameAsc();

    List<NotificationChannel> findByProviderAndDeletedFalseOrderByNameAsc(ChannelProvider provider);

    boolean existsByCodeAndDeletedFalse(String code);
}
