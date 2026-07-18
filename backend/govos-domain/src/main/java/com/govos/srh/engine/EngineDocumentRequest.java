package com.govos.srh.engine;

public record EngineDocumentRequest(
        String documentId,
        String documentJson
) {
}
