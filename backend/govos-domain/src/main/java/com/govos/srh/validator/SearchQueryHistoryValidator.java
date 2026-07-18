package com.govos.srh.validator;

import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.exception.SearchQueryException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SearchQueryHistoryValidator {

    public void validatePersist(SearchQueryHistory searchQueryHistory) {
        validateOrganizationIdRequired(searchQueryHistory.getOrganizationId());
        validateQueryTextRequired(searchQueryHistory.getQueryText());
        validateExecutionTimeNonNegative(searchQueryHistory.getExecutionTimeMs());
    }

    private void validateOrganizationIdRequired(java.util.UUID organizationId) {
        if (organizationId == null) {
            throw new SearchQueryException("Search query history organization id is required");
        }
    }

    private void validateQueryTextRequired(String queryText) {
        if (!StringUtils.hasText(queryText)) {
            throw new SearchQueryException("Search query history query text is required");
        }
    }

    private void validateExecutionTimeNonNegative(Long executionTimeMs) {
        if (executionTimeMs == null || executionTimeMs < 0) {
            throw new SearchQueryException("Search query history execution time must be zero or greater");
        }
    }
}
