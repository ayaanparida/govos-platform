package com.govos.api.ntf.search;

import java.util.UUID;

/**
 * SRH-013 extension point for NTF product search synchronization.
 * Implementation deferred until notification records require searchable metadata via SRH.
 */
public interface NotificationSearchIntegration {

    void onNotificationCreated(UUID notificationId, UUID organizationId);

    void onNotificationDelivered(UUID notificationId, UUID organizationId);

    void onNotificationArchived(UUID notificationId, UUID organizationId);
}
