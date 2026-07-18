package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasDto;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.exception.SearchAliasException;
import com.govos.srh.mapper.SearchAliasMapper;
import com.govos.srh.repository.SearchAliasRepository;
import com.govos.srh.support.SrhTestFixtures;
import com.govos.srh.validator.SearchAliasValidator;
import com.govos.srh.validator.SearchIndexValidator;
import jakarta.persistence.OptimisticLockException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAliasServiceImplTest {

    @Mock private SearchAliasRepository searchAliasRepository;
    @Mock private SearchAliasMapper searchAliasMapper;
    @Mock private SearchAliasValidator searchAliasValidator;
    @Mock private SearchIndexValidator searchIndexValidator;

    @InjectMocks
    private SearchAliasServiceImpl service;

    private SearchIndex index;
    private SearchAlias alias;
    private SearchAliasDto dto;

    @BeforeEach
    void setUp() {
        index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        alias = SrhTestFixtures.searchAlias(SrhTestFixtures.ALIAS_ID, index);
        dto = minimalDto(alias);
    }

    @Test
    void shouldCreateSearchAlias() {
        SearchAliasCreateRequest request = SrhTestFixtures.aliasCreateRequest();
        when(searchAliasMapper.toEntity(request)).thenReturn(alias);
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchAliasRepository.save(alias)).thenReturn(alias);
        when(searchAliasMapper.toDto(alias)).thenReturn(dto);

        assertThat(service.create(request)).isEqualTo(dto);
        verify(searchAliasValidator).validateCreate(request);
        assertThat(alias.getDeleted()).isFalse();
        assertThat(alias.getSearchIndex()).isEqualTo(index);
    }

    @Test
    void shouldUpdateSearchAlias() {
        SearchAliasUpdateRequest request = SrhTestFixtures.aliasUpdateRequest();
        when(searchAliasRepository.findById(SrhTestFixtures.ALIAS_ID)).thenReturn(Optional.of(alias));
        when(searchAliasRepository.save(alias)).thenReturn(alias);
        when(searchAliasMapper.toDto(alias)).thenReturn(dto);

        assertThat(service.update(SrhTestFixtures.ALIAS_ID, request)).isEqualTo(dto);
        verify(searchAliasValidator).validateUpdate(request);
        verify(searchAliasMapper).updateEntity(request, alias);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        alias.setVersion(1L);
        when(searchAliasRepository.findById(SrhTestFixtures.ALIAS_ID)).thenReturn(Optional.of(alias));

        assertThatThrownBy(() -> service.update(SrhTestFixtures.ALIAS_ID, SrhTestFixtures.aliasUpdateRequest()))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldGetByAlias() {
        when(searchAliasRepository.findByAliasNameAndDeletedFalse(SrhTestFixtures.ALIAS_NAME))
                .thenReturn(Optional.of(alias));
        when(searchAliasMapper.toDto(alias)).thenReturn(dto);

        assertThat(service.getByAlias(SrhTestFixtures.ALIAS_NAME)).isEqualTo(dto);
    }

    @Test
    void shouldListByIndex() {
        when(searchIndexValidator.requireExists(SrhTestFixtures.INDEX_ID)).thenReturn(index);
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(alias));
        when(searchAliasMapper.toDto(alias)).thenReturn(dto);

        assertThat(service.listByIndex(SrhTestFixtures.INDEX_ID)).containsExactly(dto);
    }

    @Test
    void shouldActivateAliasAndDeactivatePrevious() {
        UUID previousId = UUID.randomUUID();
        SearchAlias previous = SrhTestFixtures.searchAlias(previousId, index);
        previous.setActiveAlias(true);
        when(searchAliasRepository.findById(SrhTestFixtures.ALIAS_ID)).thenReturn(Optional.of(alias));
        when(searchAliasRepository.findAllBySearchIndexIdAndDeletedFalse(SrhTestFixtures.INDEX_ID))
                .thenReturn(List.of(previous, alias));
        when(searchAliasRepository.save(previous)).thenReturn(previous);
        when(searchAliasRepository.save(alias)).thenReturn(alias);
        when(searchAliasMapper.toDto(alias)).thenReturn(dto);

        assertThat(service.activateAlias(SrhTestFixtures.ALIAS_ID)).isEqualTo(dto);

        ArgumentCaptor<SearchAlias> captor = ArgumentCaptor.forClass(SearchAlias.class);
        verify(searchAliasRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues()).anyMatch(saved -> previousId.equals(saved.getId()) && !saved.getActiveAlias());
        assertThat(alias.getActiveAlias()).isTrue();
        assertThat(alias.getSwitchedAt()).isNotNull();
    }

    @Test
    void shouldSoftDeleteSearchAlias() {
        when(searchAliasRepository.findById(SrhTestFixtures.ALIAS_ID)).thenReturn(Optional.of(alias));

        service.softDelete(SrhTestFixtures.ALIAS_ID);

        assertThat(alias.getDeleted()).isTrue();
        assertThat(alias.getActiveAlias()).isFalse();
        verify(searchAliasRepository).save(alias);
    }

    @Test
    void shouldRestoreSearchAlias() {
        alias.setDeleted(true);
        when(searchAliasRepository.findById(SrhTestFixtures.ALIAS_ID)).thenReturn(Optional.of(alias));
        when(searchAliasRepository.save(alias)).thenReturn(alias);
        when(searchAliasMapper.toDto(alias)).thenReturn(dto);

        assertThat(service.restore(SrhTestFixtures.ALIAS_ID)).isEqualTo(dto);
        assertThat(alias.getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenAliasNotFound() {
        when(searchAliasRepository.findByAliasNameAndDeletedFalse("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAlias("missing"))
                .isInstanceOf(SearchAliasException.class);
    }

    private SearchAliasDto minimalDto(SearchAlias entity) {
        return new SearchAliasDto(
                entity.getId(), entity.getCode(), SrhTestFixtures.INDEX_ID, entity.getAliasName(),
                entity.getPhysicalIndexName(), entity.getActiveAlias(), entity.getSwitchedAt(),
                entity.getActive(), entity.getVersion(), null, null, null, null);
    }
}
