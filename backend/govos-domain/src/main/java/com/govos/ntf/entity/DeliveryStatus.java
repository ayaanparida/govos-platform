package com.govos.ntf.entity;

/**
 * Delivery lifecycle status for notification delivery records.
 * <p>
 * Enum for Sprint 0; migrate to MDM type {@code DELIVERY_STATUS} in a later sprint.
 */
public enum DeliveryStatus {
    PENDING,
    QUEUED,
    SENT,
    DELIVERED,
    FAILED,
    CANCELLED
}
