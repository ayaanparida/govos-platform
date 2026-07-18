package com.govos.srh.engine;

import java.util.List;

public record EngineFacetResult(
        String name,
        List<EngineFacetBucket> buckets
) {
}
