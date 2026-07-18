package com.govos.srh.service.impl;

import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.exception.SearchQueryException;
import com.govos.srh.mapper.SearchQueryHistoryMapper;
import com.govos.srh.repository.SearchQueryHistoryRepository;
import com.govos.srh.support.SrhTestFixtures;
import com.govos.srh.validator.SearchQueryHistoryValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchQueryHistoryServiceImplTest {

    @Mock private SearchQueryHistoryRepository searchQueryHistoryRepository;
    @Mock private SearchQueryHistoryMapper searchQueryHistoryMapper;
    @Mock private SearchQueryHistoryValidator searchQueryHistoryValidator;

    @InjectMocks
    private SearchQueryHistoryServiceImpl service;

    @Test
    void shouldRecordQueryHistory() {
        SearchQueryHistoryDto request = SrhTestFixtures.queryHistoryDto();
        SearchQueryHistory saved = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);
        SearchQueryHistoryDto dto = new SearchQueryHistoryDto(
                SrhTestFixtures.HISTORY_ID, request.code(), request.organizationId(), request.userId(),
                request.queryText(), request.queryType(), request.filtersJson(), request.resultCount(),
                request.executionTimeMs(), request.searchedAt(), true, 0L, null, null, null, null);

        when(searchQueryHistoryRepository.save(org.mockito.ArgumentMatchers.any(SearchQueryHistory.class)))
                .thenReturn(saved);
        when(searchQueryHistoryMapper.toDto(saved)).thenReturn(dto);

        assertThat(service.record(request)).isEqualTo(dto);

        ArgumentCaptor<SearchQueryHistory> captor = ArgumentCaptor.forClass(SearchQueryHistory.class);
        verify(searchQueryHistoryValidator).validatePersist(captor.capture());
        assertThat(captor.getValue().getOrganizationId()).isEqualTo(SrhTestFixtures.ORG_ID);
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldRejectRecordWhenValidationFails() {
        SearchQueryHistoryDto request = SrhTestFixtures.queryHistoryDto();
        doThrow(new SearchQueryException("invalid")).when(searchQueryHistoryValidator)
                .validatePersist(org.mockito.ArgumentMatchers.any(SearchQueryHistory.class));

        assertThatThrownBy(() -> service.record(request)).isInstanceOf(SearchQueryException.class);
    }

    @Test
    void shouldListByOrganization() {
        SearchQueryHistory entity = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);
        SearchQueryHistoryDto dto = SrhTestFixtures.queryHistoryDto();
        when(searchQueryHistoryRepository.findAllByOrganizationIdAndDeletedFalse(SrhTestFixtures.ORG_ID))
                .thenReturn(List.of(entity));
        when(searchQueryHistoryMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listByOrganization(SrhTestFixtures.ORG_ID)).containsExactly(dto);
    }

    @Test
    void shouldListByUser() {
        SearchQueryHistory entity = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);
        SearchQueryHistoryDto dto = SrhTestFixtures.queryHistoryDto();
        when(searchQueryHistoryRepository.findAllByUserIdAndDeletedFalse(SrhTestFixtures.USER_ID))
                .thenReturn(List.of(entity));
        when(searchQueryHistoryMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listByUser(SrhTestFixtures.USER_ID)).containsExactly(dto);
    }
}
