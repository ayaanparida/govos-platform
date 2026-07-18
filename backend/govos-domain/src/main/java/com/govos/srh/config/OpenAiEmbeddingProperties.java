package com.govos.srh.config;

public class OpenAiEmbeddingProperties {

    private String apiKey = "";
    private String model = "text-embedding-3-small";
    private String baseUrl = "https://api.openai.com/v1";
    private int dimension = 1536;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
