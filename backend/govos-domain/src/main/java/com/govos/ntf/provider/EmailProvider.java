package com.govos.ntf.provider;

import com.govos.ntf.entity.ChannelProvider;
import org.springframework.stereotype.Component;

@Component
public class EmailProvider implements NotificationProvider {

    private static final String NOT_IMPLEMENTED =
            "Email delivery is not implemented in Sprint 0 Day 7";

    @Override
    public ChannelProvider getProviderType() {
        return ChannelProvider.EMAIL;
    }

    @Override
    public void send(NotificationSendRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
