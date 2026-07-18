package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Aggregate root for extended document metadata and OCR extracted content (DOC-002).
 */
@Entity
@Table(
        name = "doc_document_metadata",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_document_metadata_document_id", columnList = "document_id"),
                @Index(name = "idx_doc_document_metadata_version_id", columnList = "document_version_id")
        })
public class DocumentMetadata extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_version_id")
    private DocumentVersion documentVersion;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Column(name = "ocr_language", length = 20)
    private String ocrLanguage;

    @Column(name = "ocr_confidence")
    private Double ocrConfidence;

    @Column(name = "extracted_metadata", columnDefinition = "TEXT")
    private String extractedMetadata;

    @Column(name = "custom_attributes", columnDefinition = "TEXT")
    private String customAttributes;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "language_detected", length = 20)
    private String languageDetected;

    @Column(name = "watermark_applied", nullable = false)
    private Boolean watermarkApplied = false;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public DocumentVersion getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(DocumentVersion documentVersion) {
        this.documentVersion = documentVersion;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public String getOcrLanguage() {
        return ocrLanguage;
    }

    public void setOcrLanguage(String ocrLanguage) {
        this.ocrLanguage = ocrLanguage;
    }

    public Double getOcrConfidence() {
        return ocrConfidence;
    }

    public void setOcrConfidence(Double ocrConfidence) {
        this.ocrConfidence = ocrConfidence;
    }

    public String getExtractedMetadata() {
        return extractedMetadata;
    }

    public void setExtractedMetadata(String extractedMetadata) {
        this.extractedMetadata = extractedMetadata;
    }

    public String getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(String customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getLanguageDetected() {
        return languageDetected;
    }

    public void setLanguageDetected(String languageDetected) {
        this.languageDetected = languageDetected;
    }

    public Boolean getWatermarkApplied() {
        return watermarkApplied;
    }

    public void setWatermarkApplied(Boolean watermarkApplied) {
        this.watermarkApplied = watermarkApplied;
    }
}
