package com.govos.ntf.provider;

import com.govos.ntf.entity.ChannelProvider;
import org.springframework.stereotype.Component;

@Component
public class SmsProvider implements NotificationProvider {

    private static final String NOT_IMPLEMENTED =
            "SMS delivery is not implemented in Sprint 0 Day 7";

    @Override
    public ChannelProvider getProviderType() {
        return ChannelProvider.SMS;
    }

    @Override
    public void send(NotificationSendRequest request) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
