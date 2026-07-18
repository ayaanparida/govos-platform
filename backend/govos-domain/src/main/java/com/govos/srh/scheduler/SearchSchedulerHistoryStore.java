package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Component;

@Component
public class SearchSchedulerHistoryStore {

    private final Deque<SearchScheduledJobRecord> history = new ConcurrentLinkedDeque<>();
    private final int maxEntries;

    public SearchSchedulerHistoryStore(SearchProperties searchProperties) {
        this.maxEntries = Math.max(1, searchProperties.getScheduler().getHistoryMaxEntries());
    }

    public void add(SearchScheduledJobRecord record) {
        history.addFirst(record);
        while (history.size() > maxEntries) {
            history.removeLast();
        }
    }

    public List<SearchScheduledJobRecord> list(int limit) {
        int resolvedLimit = limit <= 0 ? maxEntries : Math.min(limit, maxEntries);
        List<SearchScheduledJobRecord> records = new ArrayList<>(resolvedLimit);
        int count = 0;
        for (SearchScheduledJobRecord record : history) {
            records.add(record);
            count++;
            if (count >= resolvedLimit) {
                break;
            }
        }
        return records;
    }

    public long totalExecutions() {
        return history.size();
    }

    public long failedExecutions() {
        return history.stream()
                .filter(record -> record.getStatus() == SearchScheduledJobStatus.FAILED)
                .count();
    }

    public SearchScheduledJobRecord findLatestFailed(String jobName) {
        for (SearchScheduledJobRecord record : history) {
            if (jobName.equals(record.getJobName())
                    && record.getStatus() == SearchScheduledJobStatus.FAILED) {
                return record;
            }
        }
        return null;
    }
}
