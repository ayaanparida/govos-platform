package com.govos.srh.admin;

public interface SearchIndexMonitor {

    SearchIndexEngineStats getIndexStats(String physicalIndexName);
}
