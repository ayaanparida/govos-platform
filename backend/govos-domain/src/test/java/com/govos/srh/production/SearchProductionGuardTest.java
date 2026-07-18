package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import com.govos.srh.exception.SearchQueryException;
import com.govos.srh.query.SearchPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchProductionGuardTest {

    private SearchProductionGuard guard;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.getGuard().setMaxDeepPaginationOffset(10000);
        properties.getGuard().setMaxResultWindow(5000);
        guard = new SearchProductionGuard(properties);
    }

    @Test
    void shouldAllowValidPagination() {
        assertThatCode(() -> guard.validatePagination(new SearchPage(0, 20)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDeepPagination() {
        SearchProperties properties = new SearchProperties();
        properties.getGuard().setMaxDeepPaginationOffset(1000);
        properties.getGuard().setMaxResultWindow(5000);
        SearchProductionGuard strictGuard = new SearchProductionGuard(properties);

        assertThatThrownBy(() -> strictGuard.validatePagination(new SearchPage(200, 10)))
                .isInstanceOf(SearchQueryException.class)
                .hasMessageContaining("Deep pagination");
    }

    @Test
    void shouldRejectLargeResultWindow() {
        assertThatThrownBy(() -> guard.validatePagination(new SearchPage(50, 100)))
                .isInstanceOf(SearchQueryException.class)
                .hasMessageContaining("result window");
    }
}
