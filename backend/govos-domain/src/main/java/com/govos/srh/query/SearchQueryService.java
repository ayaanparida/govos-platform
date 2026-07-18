package com.govos.srh.query;

import java.util.List;

public interface SearchQueryService {

    SearchResponse search(SearchRequest request);

    AutocompleteResponse autocomplete(AutocompleteRequest request);

    SearchResponse geoSearch(GeoSearchRequest request);

    SearchResponse facetSearch(FacetSearchRequest request);

    long count(SearchRequest request);

    List<String> suggest(AutocompleteRequest request);
}
