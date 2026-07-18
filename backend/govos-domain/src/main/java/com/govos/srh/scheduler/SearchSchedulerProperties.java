package com.govos.srh.scheduler;

public class SearchSchedulerProperties {

    private boolean enabled = true;
    private String reindexCron = "0 0 2 * * *";
    private String incrementalReindexCron = "0 0 */6 * * *";
    private String embeddingCron = "0 30 3 * * *";
    private String cleanupCron = "0 0 4 * * *";
    private String healthCron = "0 */15 * * * *";
    private String statisticsCron = "0 5 * * * *";
    private int maxRetries = 3;
    private long initialBackoffMs = 1000L;
    private long maxBackoffMs = 30000L;
    private double backoffMultiplier = 2.0D;
    private int queryHistoryRetentionDays = 90;
    private int historyMaxEntries = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReindexCron() {
        return reindexCron;
    }

    public void setReindexCron(String reindexCron) {
        this.reindexCron = reindexCron;
    }

    public String getIncrementalReindexCron() {
        return incrementalReindexCron;
    }

    public void setIncrementalReindexCron(String incrementalReindexCron) {
        this.incrementalReindexCron = incrementalReindexCron;
    }

    public String getEmbeddingCron() {
        return embeddingCron;
    }

    public void setEmbeddingCron(String embeddingCron) {
        this.embeddingCron = embeddingCron;
    }

    public String getCleanupCron() {
        return cleanupCron;
    }

    public void setCleanupCron(String cleanupCron) {
        this.cleanupCron = cleanupCron;
    }

    public String getHealthCron() {
        return healthCron;
    }

    public void setHealthCron(String healthCron) {
        this.healthCron = healthCron;
    }

    public String getStatisticsCron() {
        return statisticsCron;
    }

    public void setStatisticsCron(String statisticsCron) {
        this.statisticsCron = statisticsCron;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getInitialBackoffMs() {
        return initialBackoffMs;
    }

    public void setInitialBackoffMs(long initialBackoffMs) {
        this.initialBackoffMs = initialBackoffMs;
    }

    public long getMaxBackoffMs() {
        return maxBackoffMs;
    }

    public void setMaxBackoffMs(long maxBackoffMs) {
        this.maxBackoffMs = maxBackoffMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public int getQueryHistoryRetentionDays() {
        return queryHistoryRetentionDays;
    }

    public void setQueryHistoryRetentionDays(int queryHistoryRetentionDays) {
        this.queryHistoryRetentionDays = queryHistoryRetentionDays;
    }

    public int getHistoryMaxEntries() {
        return historyMaxEntries;
    }

    public void setHistoryMaxEntries(int historyMaxEntries) {
        this.historyMaxEntries = historyMaxEntries;
    }
}
