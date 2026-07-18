package com.govos.srh.admin;

import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.scheduler.SearchScheduledJobRecordDto;
import com.govos.srh.scheduler.SearchSchedulerStatusDto;

import java.util.List;
import java.util.UUID;

public interface SearchAdministrationService {

    SearchHealthDto getClusterHealth();

    SearchClusterInfoDto getClusterInformation();

    List<SearchNodeInfoDto> getNodeInformation();

    SearchStatisticsDto getSearchStatistics();

    SearchIndexStatisticsDto getIndexStatistics(UUID indexId);

    SearchQueryStatisticsDto getQueryStatistics();

    List<SearchTopQueryDto> getTopQueries(int limit);

    List<SearchTopQueryDto> getTopOrganizations(int limit);

    List<SearchTopQueryDto> getTopEntityTypes(int limit);

    List<SearchTopQueryDto> getTopFilters(int limit);

    List<SearchSlowQueryDto> getSlowQueries(int limit);

    SearchSyncJobDto reindexIndex(UUID indexId);

    List<SearchSyncJobDto> reindexAll();

    SearchSyncJobDto cancelReindex(UUID jobId);

    List<SearchSyncJobDto> getRunningJobs();

    SearchDashboardDto getSearchDashboard();

    SearchSchedulerStatusDto getSchedulerStatus();

    SearchScheduledJobRecordDto triggerSchedulerReindex(boolean full);

    SearchScheduledJobRecordDto triggerSchedulerEmbedding();

    SearchScheduledJobRecordDto triggerSchedulerCache();

    SearchScheduledJobRecordDto triggerSchedulerStatistics();

    SearchScheduledJobRecordDto triggerSchedulerCleanup();

    List<SearchScheduledJobRecordDto> getSchedulerHistory(int limit);
}
