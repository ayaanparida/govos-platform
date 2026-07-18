package com.govos.srh.config;

import com.govos.srh.production.SearchCacheProperties;
import com.govos.srh.production.SearchGuardProperties;
import com.govos.srh.production.SearchMetricsProperties;
import com.govos.srh.production.SearchPoolProperties;
import com.govos.srh.production.SearchResilienceProperties;
import com.govos.srh.observability.SearchObservationProperties;
import com.govos.srh.scheduler.SearchSchedulerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "govos.search")
public class SearchProperties {

    private String host = "localhost";
    private int port = 9200;
    private String username;
    private String password;
    private boolean ssl = false;
    private int bulkBatchSize = 500;
    private int defaultPageSize = 20;
    private int maxPageSize = 100;
    private long queryTimeoutMs = 5000L;
    private SemanticSearchProperties semantic = new SemanticSearchProperties();
    private SearchResilienceProperties resilience = new SearchResilienceProperties();
    private SearchCacheProperties cache = new SearchCacheProperties();
    private SearchPoolProperties pool = new SearchPoolProperties();
    private SearchGuardProperties guard = new SearchGuardProperties();
    private SearchMetricsProperties metrics = new SearchMetricsProperties();
    private SearchSchedulerProperties scheduler = new SearchSchedulerProperties();
    private SearchObservationProperties observation = new SearchObservationProperties();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getBulkBatchSize() {
        return bulkBatchSize;
    }

    public void setBulkBatchSize(int bulkBatchSize) {
        this.bulkBatchSize = bulkBatchSize;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public long getQueryTimeoutMs() {
        return queryTimeoutMs;
    }

    public void setQueryTimeoutMs(long queryTimeoutMs) {
        this.queryTimeoutMs = queryTimeoutMs;
    }

    public SemanticSearchProperties getSemantic() {
        return semantic;
    }

    public void setSemantic(SemanticSearchProperties semantic) {
        this.semantic = semantic;
    }

    public SearchResilienceProperties getResilience() {
        return resilience;
    }

    public void setResilience(SearchResilienceProperties resilience) {
        this.resilience = resilience;
    }

    public SearchCacheProperties getCache() {
        return cache;
    }

    public void setCache(SearchCacheProperties cache) {
        this.cache = cache;
    }

    public SearchPoolProperties getPool() {
        return pool;
    }

    public void setPool(SearchPoolProperties pool) {
        this.pool = pool;
    }

    public SearchGuardProperties getGuard() {
        return guard;
    }

    public void setGuard(SearchGuardProperties guard) {
        this.guard = guard;
    }

    public SearchMetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(SearchMetricsProperties metrics) {
        this.metrics = metrics;
    }

    public SearchSchedulerProperties getScheduler() {
        return scheduler;
    }

    public void setScheduler(SearchSchedulerProperties scheduler) {
        this.scheduler = scheduler;
    }

    public SearchObservationProperties getObservation() {
        return observation;
    }

    public void setObservation(SearchObservationProperties observation) {
        this.observation = observation;
    }
}
