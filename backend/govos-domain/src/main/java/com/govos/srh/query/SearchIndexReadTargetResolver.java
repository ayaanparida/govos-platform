package com.govos.srh.query;

import com.govos.srh.dto.SearchAliasDto;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.service.SearchAliasService;
import com.govos.srh.service.SearchIndexService;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class SearchIndexReadTargetResolver {

    private final SearchIndexService searchIndexService;
    private final SearchAliasService searchAliasService;

    public SearchIndexReadTargetResolver(
            SearchIndexService searchIndexService,
            SearchAliasService searchAliasService) {
        this.searchIndexService = searchIndexService;
        this.searchAliasService = searchAliasService;
    }

    public String resolveReadTarget(String indexCode) {
        SearchIndexDto index = searchIndexService.getByCode(indexCode);
        return searchAliasService.listByIndex(index.id()).stream()
                .filter(alias -> Boolean.TRUE.equals(alias.activeAlias()))
                .sorted(Comparator.comparing(SearchAliasDto::aliasName))
                .map(SearchAliasDto::aliasName)
                .findFirst()
                .orElseGet(() -> {
                    if (index.physicalIndexName() != null && !index.physicalIndexName().isBlank()) {
                        return index.physicalIndexName();
                    }
                    return buildPhysicalIndexName(index);
                });
    }

    private String buildPhysicalIndexName(SearchIndexDto index) {
        return index.code().toLowerCase().replace('_', '-') + "_v" + index.mappingVersion();
    }
}
