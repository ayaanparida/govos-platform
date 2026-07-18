package com.govos.srh.validator;

import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.exception.SearchQueryException;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchQueryHistoryValidatorTest {

    private SearchQueryHistoryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SearchQueryHistoryValidator();
    }

    @Test
    void shouldValidatePersistWhenInputValid() {
        SearchQueryHistory history = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);

        assertThatCode(() -> validator.validatePersist(history)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingOrganizationId() {
        SearchQueryHistory history = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);
        history.setOrganizationId(null);

        assertThatThrownBy(() -> validator.validatePersist(history))
                .isInstanceOf(SearchQueryException.class);
    }

    @Test
    void shouldRejectMissingQueryText() {
        SearchQueryHistory history = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);
        history.setQueryText(" ");

        assertThatThrownBy(() -> validator.validatePersist(history))
                .isInstanceOf(SearchQueryException.class);
    }

    @Test
    void shouldRejectNegativeExecutionTime() {
        SearchQueryHistory history = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);
        history.setExecutionTimeMs(-1L);

        assertThatThrownBy(() -> validator.validatePersist(history))
                .isInstanceOf(SearchQueryException.class);
    }
}
