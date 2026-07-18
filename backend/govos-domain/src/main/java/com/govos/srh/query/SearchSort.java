package com.govos.srh.query;

public record SearchSort(
        String field,
        SortDirection direction
) {
}
