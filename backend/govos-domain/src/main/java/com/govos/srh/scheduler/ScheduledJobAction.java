package com.govos.srh.scheduler;

@FunctionalInterface
interface ScheduledJobAction {

    long execute();
}
