package com.govos.doc.storage.config;

public class LocalStorageProperties {

    private String basePath = "./data/doc-storage";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
