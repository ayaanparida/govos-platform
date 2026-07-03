package com.govos.ntf.provider;

import com.govos.ntf.entity.ChannelProvider;

/**
 * Abstraction for external notification delivery channels.
 * <p>
 * Actual delivery implementations are deferred to a later sprint.
 */
public interface NotificationProvider {

    ChannelProvider getProviderType();

    void send(NotificationSendRequest request);
}
