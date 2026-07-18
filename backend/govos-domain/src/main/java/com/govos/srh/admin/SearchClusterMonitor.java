package com.govos.srh.admin;

import java.util.List;

public interface SearchClusterMonitor {

    SearchClusterInfoDto getClusterInformation();

    List<SearchNodeInfoDto> getNodeInformation();

    SearchHealthDto getDetailedHealth(String engineStatus);
}
