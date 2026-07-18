package com.govos.audit.entity;

public enum AuditEventType {
    ENTITY_CREATED,
    ENTITY_UPDATED,
    ENTITY_DELETED,
    ENTITY_VIEWED,
    USER_LOGIN,
    USER_LOGOUT,
    PERMISSION_CHANGE,
    EXPORT,
    SYSTEM,
    OTHER
}
