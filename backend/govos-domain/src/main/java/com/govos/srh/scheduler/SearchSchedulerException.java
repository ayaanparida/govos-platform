package com.govos.srh.scheduler;

public class SearchSchedulerException extends RuntimeException {

    public SearchSchedulerException(String message) {
        super(message);
    }

    public SearchSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}
