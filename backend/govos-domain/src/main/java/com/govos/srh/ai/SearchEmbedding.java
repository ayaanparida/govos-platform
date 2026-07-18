package com.govos.srh.ai;

import java.time.Instant;
import java.util.UUID;

/**
 * In-memory embedding model for SRH-016. Not persisted via Flyway in this sprint.
 */
public class SearchEmbedding {

    private UUID embeddingId;
    private UUID referenceId;
    private UUID organizationId;
    private String entityType;
    private Integer embeddingVersion;
    private Integer vectorDimension;
    private float[] vector;
    private Instant createdDate;
    private Instant updatedDate;

    public UUID getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(UUID embeddingId) {
        this.embeddingId = embeddingId;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEmbeddingVersion() {
        return embeddingVersion;
    }

    public void setEmbeddingVersion(Integer embeddingVersion) {
        this.embeddingVersion = embeddingVersion;
    }

    public Integer getVectorDimension() {
        return vectorDimension;
    }

    public void setVectorDimension(Integer vectorDimension) {
        this.vectorDimension = vectorDimension;
    }

    public float[] getVector() {
        return vector;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }
}
