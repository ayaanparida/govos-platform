package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.enums.StorageProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Aggregate root for storage backend configuration (DOC-002).
 */
@Entity
@Table(
        name = "doc_storage_provider",
        schema = "govos",
        uniqueConstraints = @UniqueConstraint(name = "uk_doc_storage_provider_name", columnNames = "provider_name"),
        indexes = {
                @Index(name = "idx_doc_storage_provider_type", columnList = "provider_type"),
                @Index(name = "idx_doc_storage_provider_active", columnList = "active")
        })
public class StorageProvider extends AuditableEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private StorageProviderType providerType;

    @NotBlank
    @Size(max = 255)
    @Column(name = "bucket_name", nullable = false, length = 255)
    private String bucketName;

    @Size(max = 500)
    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Size(max = 100)
    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "encryption_enabled", nullable = false)
    private Boolean encryptionEnabled = true;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Size(max = 255)
    @Column(name = "secret_key_reference", length = 255)
    private String secretKeyReference;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public StorageProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(StorageProviderType providerType) {
        this.providerType = providerType;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Boolean getEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionEnabled(Boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getSecretKeyReference() {
        return secretKeyReference;
    }

    public void setSecretKeyReference(String secretKeyReference) {
        this.secretKeyReference = secretKeyReference;
    }
}
