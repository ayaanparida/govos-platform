package com.govos.srh.observability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class SearchObservationEventStore {

    private final int maxEntries;
    private final CopyOnWriteArrayList<SearchObservationEvent> events = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<SearchTraceRecord> traces = new CopyOnWriteArrayList<>();

    public SearchObservationEventStore(SearchObservationProperties properties) {
        this.maxEntries = Math.max(50, properties.getTraceHistoryMaxEntries());
    }

    public void addEvent(SearchObservationEvent event) {
        events.add(event);
        trim(events);
    }

    public void addTrace(SearchTraceRecord record) {
        traces.add(record);
        trim(traces);
    }

    public List<SearchObservationEvent> recentEvents(int limit) {
        return tail(events, limit);
    }

    public List<SearchTraceRecord> recentTraces(int limit) {
        return tail(traces, limit);
    }

    public long eventCount() {
        return events.size();
    }

    public long traceCount() {
        return traces.size();
    }

    private <T> void trim(CopyOnWriteArrayList<T> list) {
        while (list.size() > maxEntries) {
            list.remove(0);
        }
    }

    private static <T> List<T> tail(CopyOnWriteArrayList<T> list, int limit) {
        int size = list.size();
        if (size == 0) {
            return List.of();
        }
        int from = Math.max(0, size - Math.max(1, limit));
        return Collections.unmodifiableList(new ArrayList<>(list.subList(from, size)));
    }
}
