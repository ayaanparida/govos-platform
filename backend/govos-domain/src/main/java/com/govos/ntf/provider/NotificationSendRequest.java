package com.govos.ntf.provider;

/**
 * Payload for sending a notification through an external provider.
 */
public record NotificationSendRequest(
        String recipient,
        String subject,
        String body,
        String channelCode
) {
}
