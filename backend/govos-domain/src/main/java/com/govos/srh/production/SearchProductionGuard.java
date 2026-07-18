package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import com.govos.srh.exception.SearchQueryException;
import com.govos.srh.query.SearchPage;
import org.springframework.stereotype.Component;

@Component
public class SearchProductionGuard {

    private final SearchGuardProperties properties;

    public SearchProductionGuard(SearchProperties searchProperties) {
        this.properties = searchProperties.getGuard();
    }

    public void validatePagination(SearchPage page) {
        if (page == null) {
            return;
        }
        int offset = page.offset();
        if (offset > properties.getMaxDeepPaginationOffset()) {
            throw new SearchQueryException(
                    "Deep pagination offset exceeds maximum of " + properties.getMaxDeepPaginationOffset());
        }
        if (offset + page.size() > properties.getMaxResultWindow()) {
            throw new SearchQueryException(
                    "Requested result window exceeds maximum of " + properties.getMaxResultWindow());
        }
    }
}
