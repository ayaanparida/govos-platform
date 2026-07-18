package com.govos.idm.service;

import com.govos.idm.dto.CreateLoginHistoryRequest;
import com.govos.idm.dto.LoginHistoryDto;

import java.util.List;
import java.util.UUID;

public interface LoginHistoryService {

    LoginHistoryDto getById(UUID id);

    List<LoginHistoryDto> getByUserId(UUID userId);

    LoginHistoryDto record(CreateLoginHistoryRequest request);
}
