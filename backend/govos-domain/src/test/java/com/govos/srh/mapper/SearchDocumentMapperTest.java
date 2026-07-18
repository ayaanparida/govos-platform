package com.govos.srh.mapper;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchDocumentMapperTest {

    private SearchDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchDocumentMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoIncludingMetadataAndRelationships() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        SearchDocument entity = SrhTestFixtures.searchDocument(SrhTestFixtures.DOCUMENT_ID, index);
        entity.setStatus(SearchDocumentStatus.INDEXED);

        SearchDocumentDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(SrhTestFixtures.DOCUMENT_ID);
        assertThat(dto.searchIndexId()).isEqualTo(SrhTestFixtures.INDEX_ID);
        assertThat(dto.documentStatus()).isEqualTo(SearchDocumentStatus.INDEXED);
        assertThat(dto.metadataOrganizationId()).isEqualTo(SrhTestFixtures.ORG_ID);
        assertThat(dto.metadataEntityType()).isEqualTo(SrhTestFixtures.ENTITY_TYPE);
        assertThat(dto.metadataReferenceId()).isEqualTo(SrhTestFixtures.REFERENCE_ID);
        assertThat(dto.metadataMappingVersion()).isEqualTo(1);
    }

    @Test
    void shouldMapCreateRequestToEntityFlatteningMetadata() {
        SearchDocumentCreateRequest request = SrhTestFixtures.documentCreateRequest();

        SearchDocument entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getStatus()).isEqualTo(SearchDocumentStatus.NOT_INDEXED);
        assertThat(entity.getSearchIndex()).isNull();
        assertThat(entity.getEntityType()).isEqualTo(SrhTestFixtures.ENTITY_TYPE);
        assertThat(entity.getMetadata().getOrganizationId()).isEqualTo(SrhTestFixtures.ORG_ID);
        assertThat(entity.getMetadata().getMappingVersion()).isEqualTo(1);
    }

    @Test
    void shouldUpdateEntityFromRequestWithoutTouchingIgnoredFields() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        SearchDocument entity = SrhTestFixtures.searchDocument(UUID.randomUUID(), index);
        entity.setSearchDocumentId(UUID.randomUUID());
        entity.setStatus(SearchDocumentStatus.INDEXED);
        entity.setCreatedBy("creator");
        entity.setCreatedDate(Instant.parse("2026-01-01T00:00:00Z"));
        SearchDocumentUpdateRequest request = SrhTestFixtures.documentUpdateRequest();

        mapper.updateEntity(request, entity);

        assertThat(entity.getCode()).isEqualTo("DOC-001");
        assertThat(entity.getSearchDocumentId()).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(SearchDocumentStatus.INDEXED);
        assertThat(entity.getSearchIndex()).isEqualTo(index);
        assertThat(entity.getCreatedBy()).isEqualTo("creator");
        assertThat(entity.getSearchText()).isEqualTo("updated leak");
        assertThat(entity.getMetadata().getEntityType()).isEqualTo(SrhTestFixtures.ENTITY_TYPE);
    }
}
