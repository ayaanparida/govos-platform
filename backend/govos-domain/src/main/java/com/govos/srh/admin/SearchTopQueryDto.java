package com.govos.srh.admin;

public record SearchTopQueryDto(
        String queryText,
        long count,
        double averageResponseTimeMs
) {
}
