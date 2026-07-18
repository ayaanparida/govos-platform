package com.govos.srh.config;

public class SemanticSearchProperties {

    private boolean enabled = false;
    private double keywordWeight = 0.70D;
    private double vectorWeight = 0.30D;
    private String provider = "mock";
    private int topK = 20;
    private String vectorStore = "memory";
    private String vectorIndexName = "govos-vector-index";
    private int embeddingVersion = 1;
    private int embeddingBatchSize = 50;
    private int embeddingMaxRetries = 3;
    private int vectorDimension = 384;
    private OpenAiEmbeddingProperties openai = new OpenAiEmbeddingProperties();
    private AzureOpenAiEmbeddingProperties azure = new AzureOpenAiEmbeddingProperties();
    private OllamaEmbeddingProperties ollama = new OllamaEmbeddingProperties();
    private EmbeddingCacheProperties embeddingCache = new EmbeddingCacheProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getKeywordWeight() {
        return keywordWeight;
    }

    public void setKeywordWeight(double keywordWeight) {
        this.keywordWeight = keywordWeight;
    }

    public double getVectorWeight() {
        return vectorWeight;
    }

    public void setVectorWeight(double vectorWeight) {
        this.vectorWeight = vectorWeight;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public String getVectorStore() {
        return vectorStore;
    }

    public void setVectorStore(String vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String getVectorIndexName() {
        return vectorIndexName;
    }

    public void setVectorIndexName(String vectorIndexName) {
        this.vectorIndexName = vectorIndexName;
    }

    public int getEmbeddingVersion() {
        return embeddingVersion;
    }

    public void setEmbeddingVersion(int embeddingVersion) {
        this.embeddingVersion = embeddingVersion;
    }

    public int getEmbeddingBatchSize() {
        return embeddingBatchSize;
    }

    public void setEmbeddingBatchSize(int embeddingBatchSize) {
        this.embeddingBatchSize = embeddingBatchSize;
    }

    public int getEmbeddingMaxRetries() {
        return embeddingMaxRetries;
    }

    public void setEmbeddingMaxRetries(int embeddingMaxRetries) {
        this.embeddingMaxRetries = embeddingMaxRetries;
    }

    public int getVectorDimension() {
        return vectorDimension;
    }

    public void setVectorDimension(int vectorDimension) {
        this.vectorDimension = vectorDimension;
    }

    public OpenAiEmbeddingProperties getOpenai() {
        return openai;
    }

    public void setOpenai(OpenAiEmbeddingProperties openai) {
        this.openai = openai;
    }

    public AzureOpenAiEmbeddingProperties getAzure() {
        return azure;
    }

    public void setAzure(AzureOpenAiEmbeddingProperties azure) {
        this.azure = azure;
    }

    public OllamaEmbeddingProperties getOllama() {
        return ollama;
    }

    public void setOllama(OllamaEmbeddingProperties ollama) {
        this.ollama = ollama;
    }

    public EmbeddingCacheProperties getEmbeddingCache() {
        return embeddingCache;
    }

    public void setEmbeddingCache(EmbeddingCacheProperties embeddingCache) {
        this.embeddingCache = embeddingCache;
    }
}
