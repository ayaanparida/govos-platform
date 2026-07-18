package com.govos.srh.production;

public class SearchCacheProperties {

    private boolean enabled = true;
    private long ttlSeconds = 60L;
    private long maxEntries = 1000L;
    private boolean warmOnStartup = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public long getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(long maxEntries) {
        this.maxEntries = maxEntries;
    }

    public boolean isWarmOnStartup() {
        return warmOnStartup;
    }

    public void setWarmOnStartup(boolean warmOnStartup) {
        this.warmOnStartup = warmOnStartup;
    }
}
