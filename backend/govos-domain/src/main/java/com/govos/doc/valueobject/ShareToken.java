package com.govos.doc.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Share token and signed URL metadata for document sharing (DOC-002).
 */
@Embeddable
public class ShareToken {

    @Size(max = 256)
    @Column(name = "token_hash", length = 256)
    private String tokenHash;

    @Column(name = "signed_url_expires_at")
    private Instant signedUrlExpiresAt;

    @Size(max = 2048)
    @Column(name = "public_link_url", length = 2048)
    private String publicLinkUrl;

    public ShareToken() {
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getSignedUrlExpiresAt() {
        return signedUrlExpiresAt;
    }

    public void setSignedUrlExpiresAt(Instant signedUrlExpiresAt) {
        this.signedUrlExpiresAt = signedUrlExpiresAt;
    }

    public String getPublicLinkUrl() {
        return publicLinkUrl;
    }

    public void setPublicLinkUrl(String publicLinkUrl) {
        this.publicLinkUrl = publicLinkUrl;
    }
}
