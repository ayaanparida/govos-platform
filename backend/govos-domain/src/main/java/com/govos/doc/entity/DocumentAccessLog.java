package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.enums.AccessOperation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for append-only document access audit records (DOC-002).
 */
@Entity
@Table(
        name = "doc_document_access_log",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_access_log_document_id", columnList = "document_id"),
                @Index(name = "idx_doc_access_log_user_id", columnList = "user_id"),
                @Index(name = "idx_doc_access_log_accessed_at", columnList = "accessed_at"),
                @Index(name = "idx_doc_access_log_operation", columnList = "operation")
        })
public class DocumentAccessLog extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 30)
    private AccessOperation operation;

    @NotNull
    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    @Column(name = "success", nullable = false)
    private Boolean success = true;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Size(max = 500)
    @Column(name = "details", length = 500)
    private String details;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AccessOperation getOperation() {
        return operation;
    }

    public void setOperation(AccessOperation operation) {
        this.operation = operation;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(Instant accessedAt) {
        this.accessedAt = accessedAt;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
