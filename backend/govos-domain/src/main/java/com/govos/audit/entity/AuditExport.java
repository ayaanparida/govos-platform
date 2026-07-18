package com.govos.audit.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.idm.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "aud_export", schema = "govos")
public class AuditExport extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "export_type", nullable = false, length = 20)
    private AuditExportType exportType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(name = "requested_time", nullable = false)
    private Instant requestedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuditExportStatus status = AuditExportStatus.PENDING;

    @Column(name = "file_name", length = 500)
    private String fileName;

    public AuditExportType getExportType() {
        return exportType;
    }

    public void setExportType(AuditExportType exportType) {
        this.exportType = exportType;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Instant getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(Instant requestedTime) {
        this.requestedTime = requestedTime;
    }

    public AuditExportStatus getStatus() {
        return status;
    }

    public void setStatus(AuditExportStatus status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
