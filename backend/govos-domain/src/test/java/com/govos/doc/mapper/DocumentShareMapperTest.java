package com.govos.doc.mapper;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentShare;
import com.govos.doc.enums.ShareType;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentShareMapperTest {

    private DocumentShareMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentShareMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningShareToken() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentShare entity = DocumentTestFixtures.share(DocumentTestFixtures.SHARE_ID, document);
        entity.getShareToken().setPublicLinkUrl("https://example.com/share");
        entity.getShareToken().setSignedUrlExpiresAt(Instant.parse("2099-01-01T00:00:00Z"));

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.SHARE_ID);
        assertThat(response.documentId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(response.publicLinkUrl()).isEqualTo("https://example.com/share");
        assertThat(response.signedUrlExpiresAt()).isEqualTo(Instant.parse("2099-01-01T00:00:00Z"));
    }

    @Test
    void shouldMapCreateRequestToEntityWithEmbeddedShareToken() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.PUBLIC_LINK, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", "https://example.com/share", null);

        DocumentShare entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDocument()).isNull();
        assertThat(entity.getShareToken().getPublicLinkUrl()).isEqualTo("https://example.com/share");
        assertThat(entity.getShareToken().getTokenHash()).isNull();
    }

    @Test
    void shouldMapLinkResponseUsingShareId() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentShare entity = DocumentTestFixtures.share(DocumentTestFixtures.SHARE_ID, document);

        var response = mapper.toLinkResponse(entity);

        assertThat(response.shareId()).isEqualTo(DocumentTestFixtures.SHARE_ID);
        assertThat(response.documentId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
    }

    @Test
    void shouldMapSignedUrlCreateRequestToEntity() {
        CreateShareRequest request = new CreateShareRequest(
                DocumentTestFixtures.DOCUMENT_ID, ShareType.SIGNED_URL, null, null, null,
                DocumentTestFixtures.OWNER_ID, null, "READ", null, Instant.parse("2099-01-01T00:00:00Z"));

        DocumentShare entity = mapper.toEntity(request);

        assertThat(entity.getShareToken().getSignedUrlExpiresAt())
                .isEqualTo(Instant.parse("2099-01-01T00:00:00Z"));
    }

    @Test
    void shouldMapResponseListAndHandleNullEntity() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentShare entity = DocumentTestFixtures.share(DocumentTestFixtures.SHARE_ID, document);

        assertThat(mapper.toResponseList(java.util.List.of(entity))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
    }
}
