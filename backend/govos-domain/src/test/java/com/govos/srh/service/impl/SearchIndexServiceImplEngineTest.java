package com.govos.srh.service.impl;

import com.govos.srh.dto.IndexSearchDocumentRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.engine.BulkOperationResult;
import com.govos.srh.engine.EngineDocumentRequest;
import com.govos.srh.engine.SearchEngineHealthStatus;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchEngineException;
import com.govos.srh.mapper.SearchIndexMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.support.SrhTestFixtures;
import com.govos.srh.validator.SearchIndexValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchIndexServiceImplEngineTest {

    @Mock private SearchIndexRepository searchIndexRepository;
    @Mock private SearchIndexMapper searchIndexMapper;
    @Mock private SearchIndexValidator searchIndexValidator;
    @Mock private SearchEngineProvider searchEngineProvider;
    @Mock private SearchAliasRepository searchAliasRepository;
    @Mock private SearchDocumentRepository searchDocumentRepository;

    @InjectMocks
    private SearchIndexServiceImpl service;

    private SearchIndex index;

    @BeforeEach
    void setUp() {
        index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        index.setPhysicalIndexName("cmp_complaint-v1");
    }

    @Test
    void shouldCreatePhysicalIndexAfterPersistingMetadata() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchEngineProvider.indexExists("cmp_complaint-v1")).thenReturn(false);
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(toIndexDto(index));

        service.createPhysicalIndex(SrhTestFixtures.INDEX_ID);

        verify(searchIndexRepository).save(index);
        verify(searchEngineProvider).createIndex("cmp_complaint-v1");
    }

    @Test
    void shouldIndexDocumentUsingActiveAlias() {
        SearchAlias alias = new SearchAlias();
        alias.setAliasName("cmp-complaint");
        alias.setActiveAlias(true);
        when(searchIndexRepository.findByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE)).thenReturn(Optional.of(index));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(alias));

        IndexSearchDocumentRequest request = new IndexSearchDocumentRequest(
                SrhTestFixtures.INDEX_CODE,
                SrhTestFixtures.DOCUMENT_ID,
                "COMPLAINT",
                "{\"complaintId\":\"" + SrhTestFixtures.DOCUMENT_ID + "\"}");

        service.indexDocument(request);

        verify(searchEngineProvider).indexDocument(
                eq("cmp-complaint"),
                eq(SrhTestFixtures.DOCUMENT_ID.toString()),
                eq(request.documentJson()));
    }

    @Test
    void shouldRemoveDocumentFromWriteTarget() {
        when(searchIndexRepository.findByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE)).thenReturn(Optional.of(index));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of());

        service.removeDocument(SrhTestFixtures.INDEX_CODE, SrhTestFixtures.DOCUMENT_ID);

        verify(searchEngineProvider).deleteDocument("cmp_complaint-v1", SrhTestFixtures.DOCUMENT_ID.toString());
    }

    @Test
    void shouldBulkIndexDocumentsFromDtoList() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of());
        when(searchEngineProvider.bulkIndex(eq("cmp_complaint-v1"), any()))
                .thenReturn(new BulkOperationResult(1, 0));

        SearchDocumentDto document = toDocumentDto();
        BulkOperationResult result = service.bulkIndex(SrhTestFixtures.INDEX_ID, List.of(document));

        assertThat(result.successCount()).isEqualTo(1);
        ArgumentCaptor<List<EngineDocumentRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(searchEngineProvider).bulkIndex(eq("cmp_complaint-v1"), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldSwitchAliasWithBulkCopyAndArchiveOldIndex() {
        UUID aliasId = SrhTestFixtures.ALIAS_ID;
        SearchAlias alias = new SearchAlias();
        alias.setId(aliasId);
        alias.setAliasName("cmp-complaint");
        alias.setPhysicalIndexName("cmp_complaint-v1");
        alias.setSearchIndex(index);
        alias.setDeleted(false);

        SearchDocument persistedDocument = new SearchDocument();
        persistedDocument.setId(SrhTestFixtures.DOCUMENT_ID);
        persistedDocument.setSearchDocumentId(SrhTestFixtures.DOCUMENT_ID);
        persistedDocument.setDocumentJson("{\"id\":\"" + SrhTestFixtures.DOCUMENT_ID + "\"}");

        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchAliasRepository.findById(aliasId)).thenReturn(Optional.of(alias));
        when(searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(persistedDocument));
        when(searchEngineProvider.bulkIndex(eq("cmp_complaint-v2"), any()))
                .thenReturn(new BulkOperationResult(1, 0));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(alias));
        when(searchEngineProvider.indexExists("cmp_complaint-v1")).thenReturn(true);
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(toIndexDto(index));

        service.switchAlias(SrhTestFixtures.INDEX_ID, aliasId);

        verify(searchEngineProvider).createIndex("cmp_complaint-v2");
        verify(searchEngineProvider).switchAlias("cmp-complaint", "cmp_complaint-v2", "cmp_complaint-v1");
        assertThat(index.getPhysicalIndexName()).isEqualTo("cmp_complaint-v2");
        assertThat(index.getStatus()).isEqualTo(SearchIndexStatus.ARCHIVED);
        assertThat(alias.getActiveAlias()).isTrue();
    }

    @Test
    void shouldFailAliasSwitchWhenBulkCopyFails() {
        UUID aliasId = SrhTestFixtures.ALIAS_ID;
        SearchAlias alias = new SearchAlias();
        alias.setId(aliasId);
        alias.setAliasName("cmp-complaint");
        alias.setPhysicalIndexName("cmp_complaint-v1");
        alias.setSearchIndex(index);
        alias.setDeleted(false);

        SearchDocument persistedDocument = new SearchDocument();
        persistedDocument.setSearchDocumentId(SrhTestFixtures.DOCUMENT_ID);
        persistedDocument.setDocumentJson("{}");

        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchAliasRepository.findById(aliasId)).thenReturn(Optional.of(alias));
        when(searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(persistedDocument));
        when(searchEngineProvider.bulkIndex(eq("cmp_complaint-v2"), any()))
                .thenReturn(new BulkOperationResult(0, 1));

        assertThatThrownBy(() -> service.switchAlias(SrhTestFixtures.INDEX_ID, aliasId))
                .isInstanceOf(SearchEngineException.class);

        verify(searchEngineProvider, never()).switchAlias(any(), any(), any());
    }

    @Test
    void shouldReturnEngineHealthStatus() {
        when(searchEngineProvider.health()).thenReturn(SearchEngineHealthStatus.DEGRADED);

        assertThat(service.health()).isEqualTo(SearchEngineHealthStatus.DEGRADED);
    }

    private static SearchIndexDto toIndexDto(SearchIndex entity) {
        return new SearchIndexDto(
                entity.getId(), entity.getCode(), entity.getName(), entity.getDescription(),
                entity.getEngineType(), entity.getStatus(), entity.getMappingVersion(),
                entity.getPhysicalIndexName(), entity.getAliasName(), entity.getActiveDocumentCount(),
                entity.getLastReindexedAt(), entity.getActive(), entity.getVersion(),
                entity.getCreatedBy(), entity.getCreatedDate(), entity.getUpdatedBy(), entity.getUpdatedDate());
    }

    private static SearchDocumentDto toDocumentDto() {
        return new SearchDocumentDto(
                SrhTestFixtures.DOCUMENT_ID,
                "DOC-001",
                SrhTestFixtures.INDEX_ID,
                SrhTestFixtures.DOCUMENT_ID,
                SrhTestFixtures.ENTITY_TYPE,
                SrhTestFixtures.REFERENCE_ID,
                "CMP-2026-0001",
                SrhTestFixtures.ORG_ID,
                "{\"title\":\"Leak\"}",
                "water leak",
                SearchDocumentStatus.NOT_INDEXED,
                0L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                0L,
                null,
                null,
                null,
                null);
    }
}
