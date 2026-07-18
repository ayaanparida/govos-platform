package com.govos.doc.entity;

import com.govos.doc.enums.ShareType;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.valueobject.ShareToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentShareTest {

    @Test
    void shouldExposeFixtureDefaultsAndEmbeddedShareToken() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentShare entity = DocumentTestFixtures.share(DocumentTestFixtures.SHARE_ID, document);

        assertThat(entity.getDocument()).isSameAs(document);
        assertThat(entity.getShareType()).isEqualTo(ShareType.USER);
        assertThat(entity.getSharedWithUserId()).isEqualTo(DocumentTestFixtures.USER_ID);
        assertThat(entity.getCreatedById()).isEqualTo(DocumentTestFixtures.OWNER_ID);
        assertThat(entity.getPermission()).isEqualTo("READ");
        assertThat(entity.getShareToken()).isNotNull();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldUpdateShareFieldsViaSetters() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentShare entity = new DocumentShare();
        ShareToken token = new ShareToken();
        token.setPublicLinkUrl("https://example.com/share");
        token.setSignedUrlExpiresAt(Instant.parse("2099-01-01T00:00:00Z"));

        entity.setDocument(document);
        entity.setShareType(ShareType.PUBLIC_LINK);
        entity.setSharedWithEmail("user@example.com");
        entity.setExpiresAt(Instant.parse("2099-06-01T00:00:00Z"));
        entity.setPermission("WRITE");
        entity.setShareToken(token);
        entity.setDeleted(true);
        entity.setVersion(2L);

        assertThat(entity.getShareType()).isEqualTo(ShareType.PUBLIC_LINK);
        assertThat(entity.getSharedWithEmail()).isEqualTo("user@example.com");
        assertThat(entity.getShareToken().getPublicLinkUrl()).isEqualTo("https://example.com/share");
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(2L);
    }
}
