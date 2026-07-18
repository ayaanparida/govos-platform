package com.govos.doc.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShareTokenTest {

    @Test
    void shouldSupportDefaultConstructorAndSetters() {
        ShareToken token = new ShareToken();
        Instant expiresAt = Instant.parse("2099-01-01T00:00:00Z");

        token.setTokenHash("hash-value");
        token.setSignedUrlExpiresAt(expiresAt);
        token.setPublicLinkUrl("https://example.com/share");

        assertThat(token.getTokenHash()).isEqualTo("hash-value");
        assertThat(token.getSignedUrlExpiresAt()).isEqualTo(expiresAt);
        assertThat(token.getPublicLinkUrl()).isEqualTo("https://example.com/share");
    }

    @Test
    void shouldAllowNullOptionalFields() {
        ShareToken token = new ShareToken();

        assertThat(token.getTokenHash()).isNull();
        assertThat(token.getSignedUrlExpiresAt()).isNull();
        assertThat(token.getPublicLinkUrl()).isNull();
    }
}
