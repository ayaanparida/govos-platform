package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.exception.SearchDocumentException;
import com.govos.srh.mapper.SearchDocumentMapper;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.support.SrhTestFixtures;
import com.govos.srh.validator.SearchDocumentValidator;
import com.govos.srh.validator.SearchIndexValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDocumentServiceImplTest {

    @Mock private SearchDocumentRepository searchDocumentRepository;
    @Mock private SearchDocumentMapper searchDocumentMapper;
    @Mock private SearchDocumentValidator searchDocumentValidator;
    @Mock private SearchIndexValidator searchIndexValidator;

    @InjectMocks
    private SearchDocumentServiceImpl service;

    private SearchIndex index;
    private SearchDocument document;
    private SearchDocumentDto dto;

    @BeforeEach
    void setUp() {
        index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        document = SrhTestFixtures.searchDocument(SrhTestFixtures.DOCUMENT_ID, index);
        dto = minimalDto(document);
    }

    @Test
    void shouldCreateSearchDocument() {
        SearchDocumentCreateRequest request = SrhTestFixtures.documentCreateRequest();
        when(searchDocumentMapper.toEntity(request)).thenReturn(document);
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchDocumentRepository.save(document)).thenReturn(document);
        when(searchDocumentMapper.toDto(document)).thenReturn(dto);

        assertThat(service.create(request)).isEqualTo(dto);
        verify(searchDocumentValidator).validateCreate(request);
        assertThat(document.getDeleted()).isFalse();
        assertThat(document.getSearchIndex()).isEqualTo(index);
    }

    @Test
    void shouldUpdateSearchDocument() {
        SearchDocumentUpdateRequest request = SrhTestFixtures.documentUpdateRequest();
        when(searchDocumentRepository.findByIdAndDeletedFalse(SrhTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(searchDocumentRepository.save(document)).thenReturn(document);
        when(searchDocumentMapper.toDto(document)).thenReturn(dto);

        assertThat(service.update(SrhTestFixtures.DOCUMENT_ID, request)).isEqualTo(dto);
        verify(searchDocumentValidator).validateUpdate(request);
        verify(searchDocumentMapper).updateEntity(request, document);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        document.setVersion(1L);
        when(searchDocumentRepository.findByIdAndDeletedFalse(SrhTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));

        assertThatThrownBy(() -> service.update(SrhTestFixtures.DOCUMENT_ID, SrhTestFixtures.documentUpdateRequest()))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldGetById() {
        when(searchDocumentRepository.findByIdAndDeletedFalse(SrhTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(searchDocumentMapper.toDto(document)).thenReturn(dto);

        assertThat(service.getById(SrhTestFixtures.DOCUMENT_ID)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        when(searchDocumentRepository.findByIdAndDeletedFalse(SrhTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(SrhTestFixtures.DOCUMENT_ID))
                .isInstanceOf(SearchDocumentException.class);
    }

    @Test
    void shouldListByIndex() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchDocumentRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(document));
        when(searchDocumentMapper.toDto(document)).thenReturn(dto);

        assertThat(service.listByIndex(SrhTestFixtures.INDEX_ID)).containsExactly(dto);
    }

    @Test
    void shouldListByOrganization() {
        when(searchDocumentRepository.findAllByOrganizationIdAndDeletedFalse(SrhTestFixtures.ORG_ID))
                .thenReturn(List.of(document));
        when(searchDocumentMapper.toDto(document)).thenReturn(dto);

        assertThat(service.listByOrganization(SrhTestFixtures.ORG_ID)).containsExactly(dto);
    }

    @Test
    void shouldSoftDeleteSearchDocument() {
        when(searchDocumentRepository.findByIdAndDeletedFalse(SrhTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));

        service.softDelete(SrhTestFixtures.DOCUMENT_ID);

        assertThat(document.getDeleted()).isTrue();
        assertThat(document.getActive()).isFalse();
        verify(searchDocumentRepository).save(document);
    }

    @Test
    void shouldRestoreSearchDocument() {
        document.setDeleted(true);
        when(searchDocumentRepository.findById(SrhTestFixtures.DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(searchDocumentRepository.save(document)).thenReturn(document);
        when(searchDocumentMapper.toDto(document)).thenReturn(dto);

        assertThat(service.restore(SrhTestFixtures.DOCUMENT_ID)).isEqualTo(dto);
        assertThat(document.getDeleted()).isFalse();
        assertThat(document.getActive()).isTrue();
    }

    private SearchDocumentDto minimalDto(SearchDocument entity) {
        return new SearchDocumentDto(
                entity.getId(), entity.getCode(), SrhTestFixtures.INDEX_ID, entity.getSearchDocumentId(),
                entity.getEntityType(), entity.getReferenceId(), entity.getReferenceCode(),
                entity.getOrganizationId(), entity.getDocumentJson(), entity.getSearchText(),
                entity.getStatus(), entity.getSearchVersion(), entity.getIndexedAt(), entity.getLastIndexedAt(),
                SrhTestFixtures.ORG_ID, SrhTestFixtures.ENTITY_TYPE, SrhTestFixtures.REFERENCE_ID,
                "CMP-2026-0001", 1, null, null, entity.getActive(), entity.getVersion(),
                null, null, null, null);
    }
}
