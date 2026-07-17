package com.govos.cmp.enums;

/**
 * Business lifecycle status for a complaint (CMP-001.6).
 */
public enum ComplaintStatus {
    DRAFT,
    SUBMITTED,
    ASSIGNED,
    PENDING_REASSIGNMENT,
    ACCEPTED,
    IN_PROGRESS,
    WAITING_FOR_CITIZEN,
    RESOLVED,
    VERIFIED,
    CLOSED,
    ARCHIVED,
    REJECTED,
    CANCELLED,
    REOPENED
}
