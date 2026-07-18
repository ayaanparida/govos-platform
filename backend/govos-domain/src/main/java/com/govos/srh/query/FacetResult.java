package com.govos.srh.query;

import java.util.List;

public record FacetResult(
        String name,
        List<FacetBucket> buckets
) {
}
