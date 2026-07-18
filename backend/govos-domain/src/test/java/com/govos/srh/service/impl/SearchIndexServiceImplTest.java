package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.exception.SearchIndexValidationException;
import com.govos.srh.mapper.SearchIndexMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.repository.SearchDocumentRepository;
import com.govos.srh.repository.SearchIndexRepository;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.support.SrhTestFixtures;
import com.govos.srh.validator.SearchIndexValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchIndexServiceImplTest {

    @Mock private SearchIndexRepository searchIndexRepository;
    @Mock private SearchIndexMapper searchIndexMapper;
    @Mock private SearchIndexValidator searchIndexValidator;
    @Mock private SearchEngineProvider searchEngineProvider;
    @Mock private SearchAliasRepository searchAliasRepository;
    @Mock private SearchDocumentRepository searchDocumentRepository;

    @InjectMocks
    private SearchIndexServiceImpl service;

    private SearchIndex index;
    private SearchIndexDto dto;

    @BeforeEach
    void setUp() {
        index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        dto = minimalDto(index);
    }

    @Test
    void shouldCreateSearchIndex() {
        SearchIndexCreateRequest request = SrhTestFixtures.indexCreateRequest();
        when(searchIndexMapper.toEntity(request)).thenReturn(index);
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.create(request)).isEqualTo(dto);
        verify(searchIndexValidator).validateCreate(request);
        assertThat(index.getDeleted()).isFalse();
        assertThat(index.getActive()).isTrue();
        assertThat(index.getCode()).isEqualTo(SrhTestFixtures.INDEX_CODE);
    }

    @Test
    void shouldUpdateSearchIndex() {
        SearchIndexUpdateRequest request = SrhTestFixtures.indexUpdateRequest();
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.update(SrhTestFixtures.INDEX_ID, request)).isEqualTo(dto);
        verify(searchIndexValidator).validateUpdate(request);
        verify(searchIndexMapper).updateEntity(request, index);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        index.setVersion(1L);
        SearchIndexUpdateRequest request = SrhTestFixtures.indexUpdateRequest();
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);

        assertThatThrownBy(() -> service.update(SrhTestFixtures.INDEX_ID, request))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldGetById() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.getById(SrhTestFixtures.INDEX_ID)).isEqualTo(dto);
    }

    @Test
    void shouldGetByCode() {
        when(searchIndexRepository.findByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE))
                .thenReturn(Optional.of(index));
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.getByCode(SrhTestFixtures.INDEX_CODE)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByCodeNotFound() {
        when(searchIndexRepository.findByCodeAndDeletedFalse(SrhTestFixtures.INDEX_CODE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCode(SrhTestFixtures.INDEX_CODE))
                .isInstanceOf(SearchIndexNotFoundException.class);
    }

    @Test
    void shouldListSearchIndexes() {
        PageRequest pageable = PageRequest.of(0, 10);
        for (SearchIndexStatus status : SearchIndexStatus.values()) {
            when(searchIndexRepository.findAllByStatusAndDeletedFalse(status))
                    .thenReturn(status == SearchIndexStatus.ACTIVE ? List.of(index) : List.of());
        }
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        Page<SearchIndexDto> result = service.list(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSoftDeleteSearchIndex() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);

        service.softDelete(SrhTestFixtures.INDEX_ID);

        assertThat(index.getDeleted()).isTrue();
        assertThat(index.getActive()).isFalse();
        assertThat(index.getStatus()).isEqualTo(SearchIndexStatus.DELETED);
        verify(searchIndexRepository).save(index);
    }

    @Test
    void shouldRestoreSearchIndex() {
        index.setDeleted(true);
        index.setStatus(SearchIndexStatus.DELETED);
        when(searchIndexRepository.findById(SrhTestFixtures.INDEX_ID)).thenReturn(Optional.of(index));
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.restore(SrhTestFixtures.INDEX_ID)).isEqualTo(dto);
        assertThat(index.getDeleted()).isFalse();
        assertThat(index.getStatus()).isEqualTo(SearchIndexStatus.ACTIVE);
    }

    @Test
    void shouldActivateSearchIndex() {
        index.setStatus(SearchIndexStatus.ARCHIVED);
        index.setActive(false);
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.activate(SrhTestFixtures.INDEX_ID)).isEqualTo(dto);
        assertThat(index.getActive()).isTrue();
        assertThat(index.getStatus()).isEqualTo(SearchIndexStatus.ACTIVE);
    }

    @Test
    void shouldArchiveSearchIndex() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchIndexRepository.save(index)).thenReturn(index);
        when(searchIndexMapper.toDto(index)).thenReturn(dto);

        assertThat(service.archive(SrhTestFixtures.INDEX_ID)).isEqualTo(dto);
        assertThat(index.getActive()).isFalse();
        assertThat(index.getStatus()).isEqualTo(SearchIndexStatus.ARCHIVED);
    }

    @Test
    void shouldPropagateValidationFailureOnCreate() {
        SearchIndexCreateRequest request = SrhTestFixtures.indexCreateRequest();
        doThrow(new SearchIndexValidationException("invalid")).when(searchIndexValidator).validateCreate(request);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(SearchIndexValidationException.class);
        verify(searchIndexRepository, org.mockito.Mockito.never()).save(any());
    }

    private SearchIndexDto minimalDto(SearchIndex entity) {
        return new SearchIndexDto(
                entity.getId(), entity.getCode(), entity.getName(), entity.getDescription(),
                entity.getEngineType(), entity.getStatus(), entity.getMappingVersion(),
                entity.getPhysicalIndexName(), entity.getAliasName(), entity.getActiveDocumentCount(),
                entity.getLastReindexedAt(), entity.getActive(), entity.getVersion(),
                entity.getCreatedBy(), entity.getCreatedDate(), entity.getUpdatedBy(), entity.getUpdatedDate());
    }
}
