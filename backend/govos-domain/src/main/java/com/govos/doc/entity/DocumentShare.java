package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.enums.ShareType;
import com.govos.doc.valueobject.ShareToken;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for document sharing grants and links (DOC-002).
 */
@Entity
@Table(
        name = "doc_document_share",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_document_share_document_id", columnList = "document_id"),
                @Index(name = "idx_doc_document_share_recipient_id", columnList = "shared_with_user_id"),
                @Index(name = "idx_doc_document_share_expires_at", columnList = "expires_at")
        })
public class DocumentShare extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false, length = 30)
    private ShareType shareType;

    @Column(name = "shared_with_user_id")
    private UUID sharedWithUserId;

    @Column(name = "shared_with_role_id")
    private UUID sharedWithRoleId;

    @Size(max = 255)
    @Column(name = "shared_with_email", length = 255)
    private String sharedWithEmail;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Size(max = 30)
    @Column(name = "permission", nullable = false, length = 30)
    private String permission = "READ";

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "tokenHash", column = @Column(name = "token_hash", length = 256)),
            @AttributeOverride(name = "signedUrlExpiresAt", column = @Column(name = "signed_url_expires_at")),
            @AttributeOverride(name = "publicLinkUrl", column = @Column(name = "public_link_url", length = 2048))
    })
    private ShareToken shareToken;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public ShareType getShareType() {
        return shareType;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    public UUID getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(UUID sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public UUID getSharedWithRoleId() {
        return sharedWithRoleId;
    }

    public void setSharedWithRoleId(UUID sharedWithRoleId) {
        this.sharedWithRoleId = sharedWithRoleId;
    }

    public String getSharedWithEmail() {
        return sharedWithEmail;
    }

    public void setSharedWithEmail(String sharedWithEmail) {
        this.sharedWithEmail = sharedWithEmail;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public ShareToken getShareToken() {
        return shareToken;
    }

    public void setShareToken(ShareToken shareToken) {
        this.shareToken = shareToken;
    }
}
