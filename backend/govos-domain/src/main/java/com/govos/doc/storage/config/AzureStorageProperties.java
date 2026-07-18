package com.govos.doc.storage.config;

public class AzureStorageProperties {

    private String connectionString;
    private String container = "govos-documents";

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }
}
