package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;
import com.govos.doc.valueobject.DocumentChecksum;
import com.govos.doc.valueobject.FileSize;
import com.govos.doc.valueobject.StorageLocation;
import com.govos.doc.valueobject.VersionNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for immutable document version blobs (DOC-002).
 */
@Entity
@Table(
        name = "doc_document_version",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_document_version_document_id", columnList = "document_id"),
                @Index(name = "idx_doc_document_version_checksum", columnList = "checksum"),
                @Index(name = "idx_doc_document_version_storage_key", columnList = "storage_object_key"),
                @Index(name = "idx_doc_document_version_uploaded_at", columnList = "uploaded_at")
        })
public class DocumentVersion extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Embedded
    private VersionNumber versionNumber;

    @Embedded
    private DocumentChecksum checksum;

    @Embedded
    private StorageLocation storageLocation;

    @Embedded
    private FileSize fileSize;

    @NotBlank
    @Size(max = 255)
    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;

    @NotBlank
    @Size(max = 500)
    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @NotNull
    @Column(name = "uploaded_by_id", nullable = false)
    private UUID uploadedById;

    @NotNull
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_provider_id", nullable = false)
    private StorageProvider storageProvider;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "version_status", nullable = false, length = 30)
    private DocumentVersionStatus versionStatus = DocumentVersionStatus.ACTIVE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status", nullable = false, length = 30)
    private VirusScanStatus virusScanStatus = VirusScanStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false, length = 30)
    private OcrStatus ocrStatus = OcrStatus.NOT_STARTED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "preview_status", nullable = false, length = 30)
    private PreviewStatus previewStatus = PreviewStatus.NOT_GENERATED;

    @Column(name = "immutable", nullable = false)
    private Boolean immutable = true;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public VersionNumber getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(VersionNumber versionNumber) {
        this.versionNumber = versionNumber;
    }

    public DocumentChecksum getChecksum() {
        return checksum;
    }

    public void setChecksum(DocumentChecksum checksum) {
        this.checksum = checksum;
    }

    public StorageLocation getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(StorageLocation storageLocation) {
        this.storageLocation = storageLocation;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public void setFileSize(FileSize fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public UUID getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(UUID uploadedById) {
        this.uploadedById = uploadedById;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    public DocumentVersionStatus getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(DocumentVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }

    public VirusScanStatus getVirusScanStatus() {
        return virusScanStatus;
    }

    public void setVirusScanStatus(VirusScanStatus virusScanStatus) {
        this.virusScanStatus = virusScanStatus;
    }

    public OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(OcrStatus ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    public PreviewStatus getPreviewStatus() {
        return previewStatus;
    }

    public void setPreviewStatus(PreviewStatus previewStatus) {
        this.previewStatus = previewStatus;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }
}
