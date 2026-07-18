package com.govos.srh.observability;

public class SearchObservationProperties {

    private boolean enabled = true;
    private String exporter = "otlp";
    private String otlpEndpoint = "http://localhost:4317";
    private double sampleRate = 1.0;
    private boolean logSpans = false;
    private boolean logEvents = false;
    private int traceHistoryMaxEntries = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExporter() {
        return exporter;
    }

    public void setExporter(String exporter) {
        this.exporter = exporter;
    }

    public String getOtlpEndpoint() {
        return otlpEndpoint;
    }

    public void setOtlpEndpoint(String otlpEndpoint) {
        this.otlpEndpoint = otlpEndpoint;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public boolean isLogSpans() {
        return logSpans;
    }

    public void setLogSpans(boolean logSpans) {
        this.logSpans = logSpans;
    }

    public boolean isLogEvents() {
        return logEvents;
    }

    public void setLogEvents(boolean logEvents) {
        this.logEvents = logEvents;
    }

    public int getTraceHistoryMaxEntries() {
        return traceHistoryMaxEntries;
    }

    public void setTraceHistoryMaxEntries(int traceHistoryMaxEntries) {
        this.traceHistoryMaxEntries = traceHistoryMaxEntries;
    }
}
