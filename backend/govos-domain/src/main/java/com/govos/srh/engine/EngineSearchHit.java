package com.govos.srh.engine;

import java.util.List;
import java.util.Map;

public record EngineSearchHit(
        String id,
        Double score,
        Map<String, Object> source,
        Map<String, List<String>> highlights
) {
}
