package com.govos.doc.mapper;

import com.govos.doc.dto.audit.DocumentAccessLogResponse;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentAccessLog;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentAccessLogMapperTest {

    private DocumentAccessLogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentAccessLogMapperImpl();
    }

    @Test
    void shouldMapEntityToResponseFlatteningDocumentId() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentAccessLog entity = DocumentTestFixtures.accessLog(DocumentTestFixtures.SHARE_ID, document);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.SHARE_ID);
        assertThat(response.documentId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
        assertThat(response.userId()).isEqualTo(DocumentTestFixtures.USER_ID);
        assertThat(response.success()).isTrue();
    }

    @Test
    void shouldMapEntityListToResponses() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        DocumentAccessLog entity = DocumentTestFixtures.accessLog(DocumentTestFixtures.SHARE_ID, document);

        List<DocumentAccessLogResponse> responses = mapper.toResponseList(List.of(entity));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().documentId()).isEqualTo(DocumentTestFixtures.DOCUMENT_ID);
    }

    @Test
    void shouldHandleNullDocumentInResponse() {
        DocumentAccessLog entity = new DocumentAccessLog();
        entity.setId(DocumentTestFixtures.SHARE_ID);

        var response = mapper.toResponse(entity);

        assertThat(response.documentId()).isNull();
    }
}
