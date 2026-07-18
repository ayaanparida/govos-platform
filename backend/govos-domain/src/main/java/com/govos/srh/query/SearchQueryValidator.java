package com.govos.srh.query;

import com.govos.srh.config.SearchProperties;
import com.govos.srh.exception.SearchQueryException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class SearchQueryValidator {

    private final SearchProperties searchProperties;

    public SearchQueryValidator(SearchProperties searchProperties) {
        this.searchProperties = searchProperties;
    }

    public SearchPage resolvePage(SearchPage page) {
        if (page == null) {
            return new SearchPage(SearchPage.DEFAULT_PAGE, searchProperties.getDefaultPageSize());
        }
        validatePage(page);
        return page;
    }

    public void validateSearch(SearchRequest request) {
        validateIndexCode(request.indexCode());
        validateOrganizationId(request.organizationId());
        validatePage(request.page());
    }

    public void validateAutocomplete(AutocompleteRequest request) {
        validateIndexCode(request.indexCode());
        validateOrganizationId(request.organizationId());
        if (!StringUtils.hasText(request.prefix())) {
            throw new SearchQueryException("Autocomplete prefix is required");
        }
        int limit = request.limit() != null ? request.limit() : AutocompleteRequest.DEFAULT_LIMIT;
        if (limit < 1 || limit > AutocompleteRequest.MAX_LIMIT) {
            throw new SearchQueryException("Autocomplete limit must be between 1 and " + AutocompleteRequest.MAX_LIMIT);
        }
    }

    public void validateFacetSearch(FacetSearchRequest request) {
        validateIndexCode(request.indexCode());
        validateOrganizationId(request.organizationId());
    }

    public void validateGeoSearch(GeoSearchRequest request) {
        validateIndexCode(request.indexCode());
        validateOrganizationId(request.organizationId());
        if (request.latitude() == null || request.longitude() == null) {
            throw new SearchQueryException("Geo search requires latitude and longitude");
        }
        boolean hasRadius = request.radiusKm() != null && request.radiusKm() > 0;
        boolean hasBoundingBox = request.topLeftLatitude() != null
                && request.topLeftLongitude() != null
                && request.bottomRightLatitude() != null
                && request.bottomRightLongitude() != null;
        if (!hasRadius && !hasBoundingBox) {
            throw new SearchQueryException("Geo search requires radiusKm or bounding box coordinates");
        }
        validatePage(request.page());
    }

    public int resolveAutocompleteLimit(Integer limit) {
        int resolved = limit != null ? limit : AutocompleteRequest.DEFAULT_LIMIT;
        if (resolved < 1 || resolved > AutocompleteRequest.MAX_LIMIT) {
            throw new SearchQueryException("Autocomplete limit must be between 1 and " + AutocompleteRequest.MAX_LIMIT);
        }
        return resolved;
    }

    public List<String> resolveFacetFields(List<String> facetFields) {
        if (facetFields == null || facetFields.isEmpty()) {
            return List.of("status", "priority", "category", "organization", "entityType");
        }
        return facetFields;
    }

    private void validateIndexCode(String indexCode) {
        if (!StringUtils.hasText(indexCode)) {
            throw new SearchQueryException("Search index code is required");
        }
    }

    private void validateOrganizationId(java.util.UUID organizationId) {
        if (organizationId == null) {
            throw new SearchQueryException("Organization id is required for search");
        }
    }

    private void validatePage(SearchPage page) {
        if (page == null) {
            return;
        }
        if (page.page() < 0) {
            throw new SearchQueryException("Search page number must be zero or greater");
        }
        if (page.size() < 1 || page.size() > searchProperties.getMaxPageSize()) {
            throw new SearchQueryException(
                    "Search page size must be between 1 and " + searchProperties.getMaxPageSize());
        }
    }
}
